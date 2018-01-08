// Agent scout in project res

/* Initial beliefs and rules */

/* Initial goals */

/* Plans */

/* 
 * Scout tells us it is ready
 * once doctor has told it to start 
 */
+!print_ready[source(doctor)]
	: 	true
	<-	
		.print("I have recieved a command from ",doctor," to look for victims!");
		//+scout_ready;
		.wait(1000)
	.

/* 
 * Scout move to target position. 
 * Tell doctor delete old position.
 * Then tell doctor position after moving and asking a new target position.
 */		
+!next_victim(A,B)[source(doctor)]
	<- 
		 nextPosition(A,B);
	 	//.wait(1000);
		.print("I'm now at (",A,",",B,")");
		.send(doctor,untell,scoutpos(_,_));
		.send(doctor,tell,scoutpos(A,B));
		.send(doctor,achieve,visit_victims);
	.
	
/* 
 * Initial plan of localization process. 
 * After initialization process, scan around and calculate new possibilities.
 * Tell doctor number of remaining possibilities.
 */
+!localize[source(doctor)]
	<-
		initial_location;//initial possibilities
		.wait(500);
		.count(possible(_,_),W);
		.print("Initial possible positions are:",W);
		possible_location;
		.count(possible(_,_),Q);
		.print("Possible positions are:",Q);
		.send(doctor,tell,possibilitiescount(Q));
	.
	
/*
 * Moving plan for the localization process.
 * Scout move to next position and scan around then calculate new possibilities.
 * Tell doctor number of remaining possibilities.
 */
+!next_position_localization[source(doctor)]
	<-	
		loclization_move_next;	
		possible_location;
		.wait(1000);
		.count(possible(_,_),W);
		.print("Possible positions are:",W);	
		.send(doctor,tell,possibilitiescount(W));
		.send(doctor,achieve,next_position_localization);
	.
	
/*
 * Plan after the localization process is done.
 * Set the only remained possibilities as the final position.
 * Tell doctor position.
 * Update view.
 */
+!localizationdone[source(doctor)]
	<- 
		.print("Done!");
		?possible(position(E,R),direction(G,H));
		.println(E,",",R);
		//setfinaldir(G,H);
		currentpos(E,R);
		.send(doctor,tell,scoutpos(E,R));
		update_percepts;
		.send(doctor,achieve,visit_victims);
		.println(G,",",H);	
	.
	
	
	
	