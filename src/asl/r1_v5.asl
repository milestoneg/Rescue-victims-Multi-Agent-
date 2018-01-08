/* Initial beliefs */

at(P) :- pos(P,X,Y) & pos(r1,X,Y).
//originalPos(P) :- at(P).

/* Initial goal */

!check(slots).

/* Plans */

// check(slots) as before
+!check(slots) : not garbage(r1)
   <- move_Nextslot(X,Y);
      !check(slots).
+!check(slots).

// Now when we find garbage, take it to r2
+garbage(r1) : not .desire(takeGarbage(garb,r2))
   <- .print("Garbage!");
      !takeGarbage(garb,r2).
	  
// Take garbage
+!takeGarbage(G,L) : true
   <- ?pos(r1,X,Y); 
      -+pos(curr,X,Y);
	  !take(G,L);
	  !at(curr);
	  !!check(slots).
	  
// To take G to L, pick up G, make yourself at L and drop G
+!take(G,L) : true
   <- !ensure_pick(G);
      !at(L);
	  drop(G).
	  
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
