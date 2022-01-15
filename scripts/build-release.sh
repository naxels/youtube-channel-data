BASE=0.0
VERSION=$BASE.`git rev-list HEAD --count`
VVERSION=v$VERSION

echo $BASE
echo $VERSION
echo $VVERSION

./build.sh
echo "Build done"

git push

gh release create $VVERSION -t "$VVERSION" -n "$VVERSION" target/youtube-channel-data-$VERSION.jar