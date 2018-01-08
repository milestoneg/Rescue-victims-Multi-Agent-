// Version 1 of the mars rover.
//
// Just steps through the environment

/* Initial goal */

!check(slots). 

/* Plans */

// Step through the gridworld and then stop
//
// To achieve the goal !check(slots): if the robot isn't at the end of the
// world, move to the next slot, then reset the goal !check(slots)
+!check(slots) : not pos(r1,6,5)
   <- next(slot);
      !check(slots).
// Achieve the goal !check(slots) without doing anything.
//+!check(slots).

// The second clause for +!check(slots) could be replaced by:
 +!check(slots) : pos(r1,6,5)
   <- .print("Done!").
