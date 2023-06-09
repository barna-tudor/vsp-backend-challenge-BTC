# README
API made in Kotlin using Ktor and the Google Drive API.
Login database made using mongoDB, named `UserDB`, 

## Pre-launch set-up

1. Create a Google Service Account and key according to [these instructions](https://developers.google.com/identity/protocols/oauth2/service-account).
2. Save the key as a Json named `serviceAccCred.json`, placed in `src/main/resources/`
3. Create a folder inside a Google Drive, and share it to the e-mail address of the Service Account created at step 1
4. Copy the folder id to a file `folderID.txt` placed in `src/main/resources/`.
5. Set up a mongoDB database called `UserDB`

## Functionality

The API will consume the BNR endpoint once per day, including on start-up.
It then creates a file for each currency configured, named `yyyy-mm-dd-CSH.json` where CSH is the currency shorthand (e.g. EUR = euro), in the specified folder.
The file content is `{"CSH":value}`. 

By default, all 31 currencies included in the endpoint at the time of project creation will be used.
To change which currencies are saved, either manually adjust the `currentConfig.json` file found under `src/main/resources` or through the `/configureCurrencies` endpoint (requires authetication)

## Endpoints
The main 2 endpoints (`/viewData` and `/configureCurrencies`) require a basic auth header.

To register a user, send a POST request to `/register`. The body must be a Json of the format
```Json
{
  "email" : "example-email@example.com", 
  "password" : "testingpassword" 
}
```
The passwords are stored hashed and salted.

First required endpoint is `/viewData`
By default, returns a String representing a Json of type
```json
[
  {"CSH1" : 1.234},
  {"CSH2" : 5.679}
]
```
representing today's rates.

Additionally, consuming `/viewData/yyyy-mm-dd` returns a similar String, representing the rates at the imputed time.
Inputting an invalid date (Future or non-sensical e.g. `2020-16-35` returns a BadRequest)

Second endpoint, `/configureCurrencies` receives a POST with body of type application/json of format
```json
{
  "CSH1" : true,
  "CSH2" : false
}
```
Any currency not specified in the body is defaulted to `false`.
