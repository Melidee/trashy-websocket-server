# trashy-websocket-server
## THIS WEBSOCKET SERVER IS NOT COMPLIANT WITH THE RFC6455 SPECIFICATION!!! THIS CODE SHOULD NOT BE USED IN A SERIOUS ENVIRONMENT

In order to test this project you will need to connect it to a websocket client, I recommend doing this with the built in websocket client in your browser.
To do this, open the inspect panel with `right click` -> `inspect` then go to `console` to open the javascript repl
once you have started the java program, type the following message into the javascript console to start a websocket connection:
```js
let ws = new WebSocket("ws://localhost:80")
```
on the server (java) side you should see a header response and a connection message
now you can enter this line, and you should see messages being streamed from the server each second, these will be logged in the server
```js
ws.addEventListener("message", (e) => console.log(e.data))
```
in order to send a message to the client you can use the following line
```js
ws.send("Hello from client!")
```
on the server side you should see that message in the console now!

### Thank you for an amazing year of compsci mr j <3
