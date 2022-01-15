$GRAALVM_HOME/bin/native-image -jar target/youtube-channel-data-*.jar \
    --initialize-at-build-time=. \
    --enable-url-protocols=http,https \
    --no-server \
    --no-fallback \
    youtube_channel_data.core