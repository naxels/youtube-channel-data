BASE=0.0
VERSION=$BASE.`git rev-list HEAD --count`
VVERSION=v$VERSION

echo $BASE
echo $VERSION
echo $VVERSION

git tag $VVERSION