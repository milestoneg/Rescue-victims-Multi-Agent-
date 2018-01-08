/* Initial goal */

!check(slots). 

/* Plans */

// Step through to the first piece of garbage
//
// In this version, we keep moving so long as
// we don't sense garbage.
+!check(slots) : not garbage(r1)
   <- next(slot);
      !check(slots).
+!check(slots).

// If we sense garbage, say so.
+garbage(r1) : true
   <- .print("Garbage!").
