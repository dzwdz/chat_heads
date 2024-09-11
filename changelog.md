- add a new default render mode which renders chat heads directly before the player name instead of at the beginning of the line  
  this has the advantage of being less misleading when the message sender is detected wrong
- can be configured via the "Render Position" settings: "Before Line" is the old rendering, "Before Name" the new
- fix an instance where head detection for profile names with formatting codes failed (may help HaoNick servers)