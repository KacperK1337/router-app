# router-app
### Message endpoint
Messages with ipAddress, timestamp and status (AVAILABLE/GONE) can be send via POST request on http://localhost:8080/api/messages/send 
in **MessageController**. Example JSON for message:
```
{
"ipAddress": "someIp",
"messageStatus": "AVAILABLE"
}
```

### Database
App have in-memory database that will be containing Routers, where Router is an entity that have ipAddress and status that indicates if it is working properly or not (lost or has a malfunction).

### App logic
Message request from **MessageController** is passed to **MessageService** where Message is beeing created with timestamp (unix time) equal to the time it was sent. 
Based on the message ip, Router with that ip and status WORKING is created (if it doesn't already exist) via **RouterService**.

Next messages are beeing added to HashMap (ipAddress as key) and every 60 sec. a shedulded task is running that takes current Map, copies it and prepares it in **MessageUtils** by:
- removing Messages for every ipAddress that have timestamp delays for more than 30 sec.
- sorting Messages for every ipAddress by timestamp (ascending).

Prepared map copy is then passed to **RouterService** where through **RouterUtils** the status of the router is first checked based on the messages it sends. 
The returned status can be WORKING, LOST or MALFUNCTION but this does not yet mean the actual status. Based on the returned status and the current status of the Router,
in **RouterService** the actual status is selected and updated according to the following logic:
- If status was WORKING and could be LOST -> status is DISCONNECTED
- If status was WORKING and could be MALFUNCTION -> status is MALFUNCTION
- If status was DISCONNECTED and could be LOST -> status is LOST
- If status was DISCONNECTED and could be MALFUNCTION -> status is MALFUNCTION
- If status was DISCONNECTED and could be WORKING -> status is WORKING

As seen above the router may have an temporary status (WORKING/DISCONNECTED) or a permanent status (LOST/MALFUNCTION) due to the fact that messages are checked every 60 seconds, so
some of the key messages for a given ip may not be in the current copy of the map.

If router is in pernament status, its status can no longer be changed so its ip is added to Set of ignoredIps in **MessagesService**. 
In order not to clutter the map with unnecessary messages, if message contains a blocked ip, it will not be processed by app
and in response to POST request will be 401 status. 
