# Youtube-channel-data

## Goal:
Using the Youtube Data API, get all videos data from a channel through the channel's playlist and output certain data to `channel title` file, by only providing a single video-id/youtube video url from that channel.

(To start, only CSV output is supported)

The video-id / url is needed because the Youtube Data API often cannot find the channel through the channel API call.

## Motivation:
Needed to get a Youtube channel's video metadata in order to make a shared spreadsheet for quick reference / filter capabilities.
And instead of grabbing the data using web scraping, used the API.

## Usage:

First, get a Youtube Data API key from Google through:
https://console.cloud.google.com/home/dashboard

Setting the API key, either:
- Set the GOOGLE_API_KEY environment variable to the API key from Google.
  - Bash, at execution: `env GOOGLE_API_KEY=[your-api-key] ./run.sh [video-id]`
  - Powershell, at execution: Run `$env:GOOGLE_API_KEY = [your-api-key]` before ./run.sh
  - Powershell, peristent: `[Environment]::SetEnvironmentVariable("GOOGLE_API_KEY", "[your-api-key]", "User")`
- Clone the repo or download the `resources/config.edn` file from the repo and replace the :API-Key value in `resources/config.edn` with the API key from Google.

### Using the JAR:
Ensure you have Java installed (Tested with v11)

Download the latest jar from [Releases](https://github.com/naxels/youtube-channel-data/releases)

Run with:

`java -jar youtube-channel-data-(version).jar [options] (video-id / youtube video url)`

### Babashka:
Ensure you have [Babashka](https://babashka.org) installed.

Download/clone the latest youtube-channel-data source code.

In the root of the youtube-channel-data folder, run with:

`bb ycd [options] (video-id / youtube video url)`
### Output:

The resulting file will be stored in the output folder (if exists) or current folder.

## Development:

(Windows users: replace .sh with .bat)

Ensure you have Java (v1.8+) & Clojure installed: https://clojure.org/guides/getting_started#_clojure_installer_and_cli_tools

And finally either

run with:

`./run.sh [options] (video-id / youtube video url)` 

Or create a build using:

`./build.sh`

And then run with:

`java -jar target/youtube-channel-data.jar [options] (video-id / youtube video url)`

Run tests with:

`./test.sh`