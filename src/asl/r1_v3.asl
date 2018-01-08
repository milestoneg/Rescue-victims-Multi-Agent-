/* Initial goal */

!check(slots). 

/* Plans */

// The first clause stops the robot at the end of the grid.
+!check(slots) : not garbage(r1) & pos(r1, 6, 6).
+!check(slots) : not garbage(r1)
   <- next(slot);
      !check(slots).
+!check(slots).

// When we find garbage, pick it up
+garbage(r1) : true
   <- .print("Garbage!");
      !ensure_pick(garb).
	  
// Pickup works if there is garbage. Do the pick action, and call
// !ensure_pick() again to make sure it succeeds. Then check slots
// to keep moving.
+!ensure_pick(G) : garbage(r1)
   <- pick(garb);
      !ensure_pick(G);
	  !check(slots).
// This clause provides a way to !ensure_pick() if there is no garbage
// (the robot does nothing).
+!ensure_pick(_).
