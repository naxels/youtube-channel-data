# Youtube-channel-data

## Goal:
Using the Youtube Data API, get all videos data from a channel through the channel's playlist and output certain data to `channel title` file, by only providing a single video-id/youtube video url from that channel.

(To start, only CSV output is supported)

The video-id / url is needed because the Youtube Data API often cannot find the channel through the channel API call.

## Motivation:
Needed to get a Youtube channel's video metadata in order to make a shared spreadsheet for quick reference / filter capabilities.
And instead of grabbing the data using web scraping, used the API.

## Usage:

*This tool uses Bash scripts which requires a Linux/Unix distro. If you're on Windows, follow the below steps in WSL.*

First, get a Youtube Data API key from Google through:
https://console.cloud.google.com/home/dashboard

Clone the repo and replace the :API-Key value in `resources/config.edn` with the API key from Google.

Next, ensure you have Java & Clojure installed: https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools

And finally either

run with:

`./run.sh (video-id / youtube video url)` 

Or create a build using:

`./build.sh`

And then run with:

`java -jar target/youtube-channel-data-standalone.jar (video-id / youtube video url)`

The resulting file will be stored in the output folder.