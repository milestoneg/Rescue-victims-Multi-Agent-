/* Initial beliefs */

at(P) :- pos(P,X,Y) & pos(r1,X,Y).

/* Initial goal */

!check(slots).

/* Plans */

// check(slots) as before
+!check(slots) : not garbage(r1)
   <- next(slot);
      !check(slots).
+!check(slots).

// Now when we find garbage, take it to r2
+garbage(r1) : true
   <- .print("Garbage!");
      !take(garb,r2).
	  
// To take G to L, pick up G, make yourself at L and drop G
+!take(G,L) : true
   <- !ensure_pick(G);
      !at(L);
	  drop(G); !check(slots).

// ensure_pick as before
+!ensure_pick(G) : garbage(r1)
   <- pick(garb);
      !ensure_pick(G).
+!ensure_pick(_).

// If we are at L, then we have achieved the goal of being there.
+!at(L) : at(L).
// Otherwise, to be at L we move towards it, and try again to be 
// at L
+!at(L) <- ?pos(L,X,Y);
           move_towards(X,Y);
           !at(L).
