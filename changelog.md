## 1.1.0

 - add "Auto-detect Name Aliases" feature, which detects responses to `/realname` commands on EssentialsX servers and automatically adds aliases for it (on by default)
 - fix "Missing Cloth Config" screen text being invisible

## 1.0.0

This is a large rewrite of the mod - it finally works how it always should have worked!  
(And it took an insane amount of work to make it work, work.)

If you care for a bit of technical babbling with some info strewn inbetween, do read on!

The reason for this rework is that in 1.21.9 Mojang added 2 types of "object" text components:  
Atlas sprites and player sprites, which allow putting any sprite or player head inside any text - chat, menus, signs, you name it!

Chat Heads now uses these player sprite text components.

On paper this should mean massively improved mod compatibility, since it's a vanilla feature.  
In fact, it appears Chat Heads is finally fully compatible with Caxton!  
The same may be true for Modern UI's text engine.

Servers will start making use of player sprites as well.  
There was already ways to do "server-side chat heads" by using special fonts allowing to print 1 pixel at a time.  
Now it's as easy as pie and due to that, Chat Heads will not add any heads itself, if it detects any in a received message.  
(Whether this is sane behavior, time will tell)

Player sprites do however come with some limitations.

In Vanilla, they are always 8x8 pixels without any padding, which can look quite bad in chat.  
To remedy this, Chat Heads adds 1 pixel of padding, making it consistent with regular characters. This only applies to chat, so it won't mess e.g. with signs.

They also suffer from transparency issues.  
This only really affects you if you changed Minecraft's "Chat Text Opacity" setting.  
What happens is that the hat layer of your skin blends together with the face, which can make the hat layer almost invisible.  
(On that note, Chat Heads still supports the old "Before Line" render mode, which does have a transparency fix.)

Speaking of hat layers, Chat Heads now respects Minecraft's "Skin Customization" settings.  
If you take your hat off, it won't show in chat either.

Finally, almost the entire player detection logic was rewritten.  
It's hard to say if this really changes anything, but it's much more gooder code now.

While I did a ton of testing, due to the large scale of changes, issues are to be expected.  
Please report any issue you find!

 \- Fourmisain