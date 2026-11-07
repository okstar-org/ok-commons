#!/usr/bin/env bash
#
# install.sh — ok-commons 构建与发布
#
# 本目录下有【三个相互独立的 Maven 工程】，根 POM 不是聚合器（没有 <modules>），
# 两个子工程也没有声明 <parent>：
#
#   common-bom    io.github.joy1024:common-bom     packaging=pom   (BOM)
#   common-base   io.github.joy1024:common-base    packaging=jar   (import common-bom)
#   ok-commons    io.github.joy1024:ok-commons     packaging=pom   (根 POM)
#
# 所以发布顺序是固定的：common-bom → common-base → ok-commons。
# common-base 在 <dependencyManagement> 里 import 了 common-bom 的【正式版】，
# 只有该版本真正进入 Central 后 common-base 才能解析依赖完成构建；因此脚本会把
# 这个 import 版本同步改写成本次发布版本（版本号列车），否则永远构建不过。
#
# 用法:
#   ./install.sh [命令] [版本] [选项]
#
# 命令:
#   install [版本]    本地构建并安装到 ~/.m2（默认命令；本地安装跳过 GPG 签名）
#   publish <版本>    发布到 Maven Central，版本号决定走哪条通路：
#                       X.Y.Z           正式版 -> Portal 暂存 + autoPublish，不可覆盖
#                       X.Y.Z-SNAPSHOT  快照   -> 直推快照仓库，同名可覆盖
#   snapshots [模块]  查看 Central 快照仓库里已发布的版本与构建（只读，不构建）
#                       Central 已停止快照的网页浏览，故直接读 maven-metadata.xml
#                       模块写法同 --only，可省略
#   help              显示本帮助
#
# 选项:
#   --dry-run         只改写版本并本地构建校验，不 deploy；结束时回滚版本
#   --no-rollback     publish 正式版成功后不回滚版本号（pom 停在正式版）
#   --only <列表>     只处理指定模块，逗号分隔：bom,base,root
#   --skip-tests      跳过测试
#   --sign            快照也启用 GPG 签名（正式版本就始终签名）
#   --gpg-key <KEY>   GPG 私钥 ID（默认取 $GPG_KEYNAME，否则自动探测）
#   -y, --yes         跳过确认（仍然会等待 Central 发布完成）
#   --                其后参数原样透传给 mvn
#
# 环境变量:
#   MAVEN               mvn 命令，默认 mvn
#   GPG_KEYNAME         GPG 私钥指纹/ID
#
# 例子:
#   ./install.sh                      # 按当前版本本地构建安装
#   ./install.sh install 25.06.02     # 指定版本本地构建安装
#   ./install.sh publish 25.06.02     # 发布正式版到 Central（需确认）
#   ./install.sh publish 25.06.03-SNAPSHOT --yes      # 发布快照
#   ./install.sh publish 25.06.03-SNAPSHOT --only bom # 只发 common-bom 快照
#   ./install.sh publish 25.06.02 --dry-run
#   ./install.sh snapshots            # 查看三个模块的线上快照
#   ./install.sh snapshots base       # 只看 common-base
#
# 注意: 本工作副本的 .git 指向一个不存在的 gitdir（子模块残留），git 不可用，
#       因此脚本不做任何 commit / tag / push，只做构建与发布。
#
set -euo pipefail

# --- 定位脚本目录（兼容符号链接） ---------------------------------------------
SOURCE="${BASH_SOURCE[0]}"
while [ -L "$SOURCE" ]; do
    DIR="$(cd -P "$(dirname "$SOURCE")" >/dev/null 2>&1 && pwd)"
    SOURCE="$(readlink "$SOURCE")"
    [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE"
done
ROOT="$(cd -P "$(dirname "$SOURCE")" >/dev/null 2>&1 && pwd)"
MVN="${MAVEN:-mvn}"

PORTAL_URL="https://central.sonatype.com/publishing/deployments"
SNAPSHOT_URL="https://central.sonatype.com/repository/maven-snapshots/"
GROUP_ID="io.github.joy1024"

# --- 模块定义 -----------------------------------------------------------------
# 顺序即发布顺序：common-bom 必须先真正入库，common-base 才解析得到。
ORDER=(bom base root)

module_dir()   { case "$1" in bom) echo "common-bom"  ;; base) echo "common-base" ;; root) echo "."        ;; esac; }
module_pom()   { case "$1" in bom) echo "common-bom/pom.xml" ;; base) echo "common-base/pom.xml" ;; root) echo "pom.xml" ;; esac; }
module_label() { case "$1" in bom) echo "common-bom"  ;; base) echo "common-base" ;; root) echo "ok-commons" ;; esac; }

# --- 日志 ---------------------------------------------------------------------
info() { printf '\033[32m[ok-commons]\033[0m %s\n' "$*"; }
warn() { printf '\033[33m[ok-commons]\033[0m %s\n' "$*" >&2; }
die()  { printf '\033[31m[ok-commons]\033[0m %s\n' "$*" >&2; exit 1; }

# --- 版本号读写 ---------------------------------------------------------------
# 读取 POM 的 project version。三个 POM 都没有 <parent>，因此文件里第一个
# <version> 就是工程版本；若将来引入 <parent>，这里会直接报错而不是猜错。
pom_version() {
    python3 - "$1" <<'PY'
import re, sys
path = sys.argv[1]
src = open(path, encoding='utf-8').read()
body = re.sub(r'<!--.*?-->', '', src, flags=re.S)
if re.search(r'<parent\b', body):
    sys.exit(f'{path}: 含 <parent>，脚本的版本解析不再适用，请改用 mvn help:evaluate')
m = re.search(r'<version>\s*([^<\s]+)\s*</version>', body)
if not m:
    sys.exit(f'{path}: 未找到 <version>')
print(m.group(1))
PY
}

# common-base 里 import 的 common-bom 版本（该依赖不在同一 reactor，versions:set 不会动它）
bom_pin() {
    python3 - "$ROOT/common-base/pom.xml" <<'PY'
import re, sys
src = open(sys.argv[1], encoding='utf-8').read()
m = re.search(r'<groupId>\s*io\.github\.joy1024\s*</groupId>\s*'
              r'<artifactId>\s*common-bom\s*</artifactId>\s*'
              r'<version>([^<]+)</version>', src)
print(m.group(1).strip() if m else '')
PY
}

set_bom_pin() {
    python3 - "$ROOT/common-base/pom.xml" "$1" <<'PY'
import re, sys
path, ver = sys.argv[1], sys.argv[2]
src = open(path, encoding='utf-8').read()
pat = re.compile(r'(<groupId>\s*io\.github\.joy1024\s*</groupId>\s*'
                 r'<artifactId>\s*common-bom\s*</artifactId>\s*'
                 r'<version>)[^<]*(</version>)')
new, n = pat.subn(lambda m: m.group(1) + ver + m.group(2), src)
if n != 1:
    sys.exit(f'{path}: 期望命中 1 处 common-bom import，实际 {n} 处')
open(path, 'w', encoding='utf-8').write(new)
PY
}

# 直接改写 POM 的 project version。
# 不用 mvn versions:set：它需要先解析整个模型，而 common-base import 的
# common-bom 版本在首轮发布前并不存在，解析必然失败——恰恰是回滚最需要的时刻。
# 这三个 POM 都没有 <parent>，文件里第一个 <version> 就是工程版本。
set_pom_version() {  # $1=pom 相对路径  $2=版本
    python3 - "$ROOT/$1" "$2" <<'PY'
import re, sys
path, ver = sys.argv[1], sys.argv[2]
src = open(path, encoding='utf-8').read()
# 把注释替换成等长空白，保证匹配偏移量仍可用于原文件
body = re.sub(r'<!--.*?-->', lambda m: ' ' * len(m.group(0)), src, flags=re.S)
if re.search(r'<parent\b', body):
    sys.exit(f'{path}: 含 <parent>，脚本的版本改写不再适用')
m = re.search(r'<version>\s*[^<\s]+\s*</version>', body)
if not m:
    sys.exit(f'{path}: 未找到 <version>')
open(path, 'w', encoding='utf-8').write(
    src[:m.start()] + f'<version>{ver}</version>' + src[m.end():])
PY
}

# 模块 key -> 改写版本
set_module_version() {  # $1=模块 key  $2=版本
    set_pom_version "$(module_pom "$1")" "$2"
}

# 25.06.02 -> 25.06.03-SNAPSHOT
next_snapshot() {
    local v="$1" prefix last
    prefix="${v%.*}"; last="${v##*.}"
    [[ "$v" == *.* && "$last" =~ ^[0-9]+$ ]] || return 1
    printf '%s.%0*d-SNAPSHOT' "$prefix" "${#last}" "$(( 10#$last + 1 ))"
}

assert_versions() {  # $1=期望版本
    local m v
    for m in "${SCOPE[@]}"; do
        v="$(pom_version "$ROOT/$(module_pom "$m")")"
        [ "$v" = "$1" ] || die "$(module_pom "$m") 版本为 $v，期望 $1"
    done
    if in_scope base; then
        v="$(bom_pin)"
        [ "$v" = "$1" ] || die "common-base 的 common-bom import 为 $v，期望 $1"
    fi
}

# --- GPG 私钥 -----------------------------------------------------------------
detect_gpg_key() {
    command -v gpg >/dev/null 2>&1 || return 1
    gpg --list-secret-keys --with-colons 2>/dev/null | awk -F: '
        /^sec:/ { want = ($2 == "u" || $2 == "f"); next }
        /^fpr:/ && want { print $10; exit }'
}

# --- 参数 ---------------------------------------------------------------------
CMD=""; VERSION=""
DRY_RUN=0; NO_ROLLBACK=0; SKIP_TESTS=0; ASSUME_YES=0; FORCE_SIGN=0
ONLY=""; GPG_KEY="${GPG_KEYNAME:-}"
MVN_EXTRA=()

# 帮助文本就是文件头注释：取第 2 行到 `set -euo` 之前的最后一行
usage() {
    local end
    end="$(grep -n '^set -euo' "$0" | head -1 | cut -d: -f1)"
    sed -n "2,$((end - 2))p" "$0" | sed 's/^# \{0,1\}//'
}

while [ $# -gt 0 ]; do
    case "$1" in
        -h|--help|help) usage; exit 0 ;;
        install|publish|snapshots) CMD="$1" ;;
        --dry-run)        DRY_RUN=1 ;;
        --no-rollback)    NO_ROLLBACK=1 ;;
        --skip-tests)     SKIP_TESTS=1 ;;
        --sign)           FORCE_SIGN=1 ;;
        -y|--yes)         ASSUME_YES=1 ;;
        --only)           shift; [ $# -ge 1 ] || die "--only 缺少参数"; ONLY="$1" ;;
        --gpg-key)        shift; [ $# -ge 1 ] || die "--gpg-key 缺少参数"; GPG_KEY="$1" ;;
        --)               shift; MVN_EXTRA=("$@"); break ;;
        -*)               die "未知选项: $1（用 help 查看用法）" ;;
        *)                if [ -z "$VERSION" ]; then VERSION="$1"; else die "多余参数: $1"; fi ;;
    esac
    shift
done

[ -n "$CMD" ] || CMD="install"

# snapshots 的位置参数是模块名（复用 --only 的解析），不是版本号；它也不构建，
# 因此不要求 maven 存在——只读远端 metadata，有 python3 就够。
if [ "$CMD" = snapshots ] && [ -n "$VERSION" ]; then
    [ -n "$ONLY" ] && die "snapshots 的位置参数与 --only 冲突，二选一"
    ONLY="$VERSION"; VERSION=""
fi

if [ "$CMD" != snapshots ]; then
    command -v "$MVN" >/dev/null 2>&1 || die "未找到 maven 命令: $MVN"
fi

# --- 计算处理范围 -------------------------------------------------------------
SCOPE=("${ORDER[@]}")
if [ -n "$ONLY" ]; then
    SCOPE=()
    IFS=',' read -r -a _want <<< "$ONLY"
    for w in "${_want[@]}"; do
        w="${w// /}"
        case "$w" in
            bom|base|root)             SCOPE+=("$w") ;;
            common-bom)                SCOPE+=(bom)  ;;
            common-base)               SCOPE+=(base) ;;
            ok-commons)                SCOPE+=(root) ;;
            *) die "模块只接受 bom,base,root（也认 common-bom/common-base/ok-commons），收到: $w" ;;
        esac
    done
    [ "${#SCOPE[@]}" -gt 0 ] || die "--only 未解析出任何模块"
    # 保持固定顺序，避免用户写成 base,bom 时顺序错乱
    _sorted=()
    for m in "${ORDER[@]}"; do
        for s in "${SCOPE[@]}"; do [ "$m" = "$s" ] && _sorted+=("$m"); done
    done
    SCOPE=("${_sorted[@]}")
fi

in_scope() {
    local s
    for s in "${SCOPE[@]}"; do [ "$s" = "$1" ] && return 0; done
    return 1
}

# --- 版本回滚 -----------------------------------------------------------------
ROLLBACK_NEEDED=0
declare -A ORIG_VERSION=()
ORIG_PIN=""

# 在改写版本之前调用，记录各模块当前版本，失败时据此还原
snapshot_versions() {
    local m
    for m in "${SCOPE[@]}"; do
        ORIG_VERSION[$m]="$(pom_version "$ROOT/$(module_pom "$m")")"
    done
    in_scope base && ORIG_PIN="$(bom_pin)"
    return 0
}

rollback() {  # $1=可选的提示语
    [ "$ROLLBACK_NEEDED" = 1 ] || return 0
    ROLLBACK_NEEDED=0
    trap - EXIT
    warn "${1:-构建未成功，回滚版本号 ...}"
    local m
    for m in "${SCOPE[@]}"; do
        set_module_version "$m" "${ORIG_VERSION[$m]}" \
            || warn "回滚 $(module_pom "$m") 失败，请手动改回 ${ORIG_VERSION[$m]}"
    done
    if in_scope base && [ -n "$ORIG_PIN" ]; then
        set_bom_pin "$ORIG_PIN" \
            || warn "回滚 common-base 的 common-bom import 失败，请手动改回 $ORIG_PIN"
    fi
    info "已回滚到改写前的版本号"
}

on_exit() {
    local rc=$?
    if [ "$rc" -ne 0 ]; then rollback; fi
    exit "$rc"
}
trap on_exit EXIT
# 信号也需要回滚：EXIT trap 在收到未被捕获的信号时不会执行
trap 'exit 130' INT
trap 'exit 143' TERM

# --- 改写版本（含 pin） -------------------------------------------------------
apply_versions() {  # $1=目标版本
    local m
    for m in "${SCOPE[@]}"; do
        info "$(module_label "$m"): 版本 -> $1"
        set_module_version "$m" "$1"
    done
    if in_scope base; then
        local old; old="$(bom_pin)"
        if [ "$old" != "$1" ]; then
            info "common-base: common-bom import $old -> $1"
            set_bom_pin "$1"
        fi
    fi
}

# --- mvn 公共参数 -------------------------------------------------------------
mvn_common() {
    MVN_COMMON=(--batch-mode --no-transfer-progress)
    if [ "$SKIP_TESTS" = 1 ]; then
        MVN_COMMON+=(-Dmaven.test.skip=true)
    fi
    return 0
}

# =============================================================================
# install
# =============================================================================
do_install() {
    local target="$VERSION"
    [ -n "$target" ] || target="$(pom_version "$ROOT/pom.xml")"
    [[ "$target" =~ ^[0-9][0-9A-Za-z._-]*$ ]] \
        || die "无法识别的参数: $target（命令需为 install|publish|snapshots|help，版本号需形如 25.06.02）"

    if in_scope base && ! in_scope bom; then
        warn "范围含 common-base 但不含 common-bom："\
"假定 io.github.joy1024:common-bom:$target 已存在于本地仓库，否则 common-base 解析依赖会失败"
    fi

    mvn_common
    info "本地构建安装，版本 $target，模块: ${SCOPE[*]}"
    snapshot_versions
    ROLLBACK_NEEDED=1
    apply_versions "$target"
    assert_versions "$target"

    local m
    for m in "${SCOPE[@]}"; do
        info "$(module_label "$m"): clean install"
        # 本地安装不签名：common-bom/common-base 的 gpg 绑定在 verify 阶段，
        # 且不像根 POM 那样被 profile 隔离，不跳过会直接构建失败。
        ( cd "$ROOT/$(module_dir "$m")" && "$MVN" "${MVN_COMMON[@]}" clean install \
            -Dgpg.skip=true "${MVN_EXTRA[@]}" )
    done
    ROLLBACK_NEEDED=0
    info "完成：已安装到 ~/.m2，版本 $target"
}

# =============================================================================
# publish
# =============================================================================
do_publish() {
    [ -n "$VERSION" ] || die "publish 需要显式版本号，例如: ./install.sh publish 25.06.02"

    # 正式版 X.Y.Z / 快照版 X.Y.Z-SNAPSHOT，两者走完全不同的通路：
    #   正式版 -> Central Portal 暂存区，autoPublish 后入库，且不可删除、不可覆盖
    #   快照版 -> 插件把产物 defer 后直接推到 centralSnapshotsUrl，同名可覆盖
    local is_snapshot=0
    case "$VERSION" in
        *-SNAPSHOT)
            is_snapshot=1
            [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+-SNAPSHOT$ ]] \
                || die "快照版本号需形如 25.06.02-SNAPSHOT：$VERSION" ;;
        *)
            [[ "$VERSION" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]] \
                || die "版本号需形如 25.06.02 或 25.06.02-SNAPSHOT：$VERSION" ;;
    esac

    # 下一版快照号只有正式版才需要
    local next=""
    if [ "$is_snapshot" = 0 ]; then
        next="$(next_snapshot "$VERSION")" || die "无法由 $VERSION 推导下一版快照号"
    fi

    # 签名：正式版必签；快照默认跳过（Central 快照仓库不强制），--sign 可强制
    local sign=1
    [ "$is_snapshot" = 1 ] && sign=0
    [ "$FORCE_SIGN" = 1 ] && sign=1
    if [ "$sign" = 1 ]; then
        [ -n "$GPG_KEY" ] || GPG_KEY="$(detect_gpg_key || true)"
        [ -n "$GPG_KEY" ] || die "未探测到可用 GPG 私钥，请设置 GPG_KEYNAME 或传 --gpg-key"
        gpg --list-secret-keys "$GPG_KEY" >/dev/null 2>&1 \
            || die "GPG 私钥不可用: $GPG_KEY"
    fi

    mvn_common

    if in_scope base && ! in_scope bom; then
        warn "范围含 common-base 但不含 common-bom："\
"需要 io.github.joy1024:common-bom:$VERSION 已存在于 Central，否则 common-base 解析依赖会失败"
    fi

    # 发布前提示
    printf '\n'
    if [ "$is_snapshot" = 1 ]; then
        info "即将发布快照（同名快照会被覆盖）："
    else
        info "即将发布以下坐标到 Maven Central（正式版发布后不可删除、不可覆盖）："
    fi
    local m
    for m in "${SCOPE[@]}"; do
        printf '    io.github.joy1024:%-14s %s\n' "$(module_label "$m")" "$VERSION"
    done
    printf '\n'
    info "发布顺序    : ${SCOPE[*]}（common-bom 先入库，common-base 才解析得到）"
    if [ "$is_snapshot" = 1 ]; then
        info "发布通路    : Central 快照仓库（插件 defer 后直推，无 Portal 暂存环节）"
        info "签名        : $([ "$sign" = 1 ] && echo "启用 ($GPG_KEY)" || echo "跳过（如需启用加 --sign）")"
        info "版本回滚    : 否（快照不做版本号回滚）"
    else
        info "发布通路    : Central Portal，-DautoPublish=true -DwaitUntil=PUBLISHED"
        info "签名        : 启用 ($GPG_KEY)"
    fi
    if [ "$DRY_RUN" = 1 ]; then
        info "dry-run     : 只本地构建校验，不 deploy"
    elif [ "$is_snapshot" = 0 ]; then
        if [ "$NO_ROLLBACK" = 1 ]; then
            info "版本回滚    : 否（pom 将停在 $VERSION）"
        else
            info "版本回滚    : 是（成功后将回滚到 $next）"
        fi
    fi
    printf '\n'

    if [ "$ASSUME_YES" != 1 ]; then
        if [ "$DRY_RUN" = 1 ]; then
            printf '执行 dry-run？输入 y 继续: '
        else
            printf '确认发布？输入 y 继续: '
        fi
        local ans=""; read -r ans
        case "$ans" in y|Y|yes|YES) ;; *) die "已取消" ;; esac
    fi

    snapshot_versions
    ROLLBACK_NEEDED=1
    apply_versions "$VERSION"
    assert_versions "$VERSION"

    if [ "$DRY_RUN" = 1 ]; then
        # bom 必须先 install 到本地，common-base 才解析得到它的 import
        for m in "${SCOPE[@]}"; do
            local goal="package"
            [ "$m" = "bom" ] && goal="install"
            info "$(module_label "$m"): dry-run clean $goal（跳过签名）"
            ( cd "$ROOT/$(module_dir "$m")" && "$MVN" "${MVN_COMMON[@]}" clean "$goal" \
                -Dgpg.skip=true "${MVN_EXTRA[@]}" )
        done
        info "dry-run 通过（common-bom 已留存在本地仓库，供 common-base 解析）"
        rollback "dry-run 结束，回滚版本号 ..."
        return 0
    fi

    # 正式发布
    for m in "${SCOPE[@]}"; do
        local dir; dir="$(module_dir "$m")"
        local -a extra=()
        [ "$m" = "root" ] && extra+=(-Pcentral)   # 根 POM 的发布配置在 <profile>central</profile> 里

        if [ "$is_snapshot" = 1 ]; then
            local -a signarg=(-Dgpg.skip=true)
            [ "$sign" = 1 ] && signarg=(-Dgpg.keyname="$GPG_KEY")
            info "$(module_label "$m"): clean deploy（快照）"
            ( cd "$ROOT/$dir" && "$MVN" "${MVN_COMMON[@]}" clean deploy \
                "${signarg[@]}" "${extra[@]}" "${MVN_EXTRA[@]}" )
        else
            info "$(module_label "$m"): clean deploy -DautoPublish=true -DwaitUntil=PUBLISHED"
            ( cd "$ROOT/$dir" && "$MVN" "${MVN_COMMON[@]}" clean deploy \
                -DautoPublish=true -DwaitUntil=PUBLISHED \
                -Dgpg.keyname="$GPG_KEY" "${extra[@]}" "${MVN_EXTRA[@]}" )
        fi
        info "$(module_label "$m") 已发布"
    done

    ROLLBACK_NEEDED=0
    printf '\n'
    if [ "$is_snapshot" = 1 ]; then
        info "快照发布完成，版本 $VERSION（pom 保持不变）"
        printf '\n'
        info "消费方在 pom 里引用 ${VERSION} 即可，仓库地址："
        printf '    %s\n' "$SNAPSHOT_URL"
        printf '    （根 POM 的 <repositories> 已配置 id=sonatype-snapshots 指向该地址）\n'
        return 0
    fi

    info "全部发布完成，版本 $VERSION"

    if [ "$NO_ROLLBACK" != 1 ]; then
        info "回滚版本号到 $next（仅改 pom，提交请自行处理）"
        apply_versions "$next"
    fi

    printf '\n'
    info "后续人工步骤："
    printf '    1. 在 Central Portal 确认部署状态: %s\n' "$PORTAL_URL"
    printf '    2. 提交 pom 改动并打标签（本工作副本 git 不可用，需在可用副本中操作）\n'
}

# =============================================================================
# snapshots
# =============================================================================
# Central 没有快照浏览界面——官方在 /repository/maven-snapshots/ 上明确写着
# "SNAPSHOT browsing ... unavailable"，search.maven.org 也完全不索引快照。
# 唯一可靠的入口是仓库自己的 maven-metadata.xml，两个层级各有用途：
#   模块级 <artifact>/maven-metadata.xml            -> 该模块有哪些 SNAPSHOT 版本
#   版本级 <artifact>/<ver>/maven-metadata.xml      -> 每次构建的时间戳 / buildNumber
# 输出 6 个制表符分隔的字段：模块 版本 实际版本号 build 更新时间 产物
snapshots_query() {  # $1=显示名  $2=artifactId
    python3 - "$SNAPSHOT_URL" "$GROUP_ID" "$1" "$2" <<'PY'
import re, sys, urllib.request, urllib.error, xml.etree.ElementTree as ET
from datetime import datetime, timezone

base, group, label, artifact = sys.argv[1:5]
root = f'{base}{group.replace(".", "/")}/{artifact}'


def get(url):
    req = urllib.request.Request(url, headers={'User-Agent': 'ok-commons-install.sh'})
    try:
        with urllib.request.urlopen(req, timeout=20) as r:
            return r.read()
    except urllib.error.HTTPError as e:
        if e.code == 404:
            return None
        sys.exit(f'查询失败 HTTP {e.code}: {url}')
    except urllib.error.URLError as e:
        sys.exit(f'无法访问 {url}: {e.reason}')


def text(el, tag):
    n = el.find(tag) if el is not None else None
    return n.text.strip() if n is not None and n.text else ''


def local(v):  # 20260914.020326 是 UTC，转成本地时间便于对照
    try:
        return datetime.strptime(v, '%Y%m%d.%H%M%S').replace(
            tzinfo=timezone.utc).astimezone().strftime('%Y-%m-%d %H:%M:%S')
    except ValueError:
        return '-'


def emit(ver, value, build, when, files):
    print(f'{label}\t{ver}\t{value}\t{build}\t{when}\t{files}')


raw = get(f'{root}/maven-metadata.xml')
versions = [] if raw is None else [
    v.text.strip() for v in ET.fromstring(raw).iter('version') if v.text]

if not versions:
    emit('-', '-', '-', '-', '未发布')
    sys.exit(0)

for ver in versions:
    vraw = get(f'{root}/{ver}/maven-metadata.xml')
    if vraw is None:
        emit(ver, '-', '-', '-', '缺版本级 metadata')
        continue
    m = ET.fromstring(vraw)
    snap = m.find('versioning/snapshot')
    stamp = text(snap, 'timestamp')
    build = text(snap, 'buildNumber')
    value, files = '', []
    for sv in m.iter('snapshotVersion'):
        ext, cls = text(sv, 'extension'), text(sv, 'classifier')
        name = f'{ext}:{cls}' if cls else ext
        if name not in files:
            files.append(name)
        if not value:
            value = text(sv, 'value')
    emit(ver, value or '-', f'#{build}' if build else '-',
         local(stamp), ', '.join(files) or '-')
PY
}

do_snapshots() {
    command -v python3 >/dev/null 2>&1 || die "snapshots 需要 python3"

    printf '\n'
    info "Central 快照仓库（无网页浏览，此处直接读 maven-metadata.xml）："
    printf '    %s%s/\n\n' "$SNAPSHOT_URL" "${GROUP_ID//.//}"

    # 表头用 ASCII：中文是双宽字符，printf 的 %-Ns 对齐会错位
    printf '%-13s %-20s %-30s %-6s %-21s %s\n' \
        'MODULE' 'VERSION' 'RESOLVED' 'BUILD' 'UPDATED (local)' 'FILES'
    printf -- '-%.0s' {1..120}; printf '\n'

    local m out
    for m in "${SCOPE[@]}"; do
        out="$(snapshots_query "$(module_label "$m")" "$(module_label "$m")")"
        while IFS=$'\t' read -r label ver value build when files; do
            printf '%-13s %-20s %-30s %-6s %-21s %s\n' \
                "$label" "$ver" "$value" "$build" "$when" "$files"
        done <<< "$out"
    done

    printf '\n'
    info "消费方引用 ${GROUP_ID}:<模块>:<版本> 即可；同名快照总是指向最新一次构建"
}

# =============================================================================
case "$CMD" in
    install)   do_install ;;
    publish)   do_publish ;;
    snapshots) do_snapshots ;;
    *)         die "未知命令: $CMD" ;;
esac
