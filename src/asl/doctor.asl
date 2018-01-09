// Agent doctor in project mars

/* Initial beliefs and rules */
possibilitiescount(999).//The initial value is better more than the total possibilities

//believes of obstacles
// obstacle locations for the 6*6 arena
obstaclelist([obstacle(1,6),obstacle(2,3),obstacle(3,2),obstacle(5,2),obstacle(5,5),obstacle(6,5)]).

// obstacle locations for 5*6 arena
//obstaclelist([obstacle(1,3),obstacle(2,2),obstacle(4,2),obstacle(4,5),obstacle(5,5)]).

//believes of possible victims
//victim list for 6*6 arena
victimlist([possiblevictim(1,1),possiblevictim(5,1),possiblevictim(3,3),possiblevictim(4,4),possiblevictim(3,5)]).

//victim list for 5*6 arena
//victimlist([possiblevictim(1,4),possiblevictim(2,3),possiblevictim(2,5),possiblevictim(4,1),possiblevictim(1,1)]).

/* Initial goals */
!start.
/* Plans */

/*
 * Mission start plan.
 * Send command to scout to be ready.
 * Start localization initial plan.
 */
+!start
	<-	
		?obstaclelist(L);
		?victimlist(M);
		constructenv(L,M);
		.print("I have ordered scout to look the victims!");
		.send(scout,achieve,print_ready)		
		.wait(1000);
		!localizationinit
	.
	
/*
 * If path list is empty then print done.
 * Send a command to robot to make some noise.
 */
+!visit_victims: path(X) &  .empty(X)
	<-
		.print("Done");
  		 robotbeep
  	. 

/*
 * If victim founded is equal to 3 or more than 3 then print done.
 * Send a command to robot to make some noise.
 */
+!visit_victims: victimcount(I) & I >= 3
<-.print("Done");
   robotbeep.   
 
/*
 * Plan used to visit all possible victim positions.
 * Execute only if path list is not empty and victim founded is less than 3.
 * Find new target from path list and send to scout.
 * 
 */
+!visit_victims: path(X) & not .empty(X) 
	<- 
	   ?victimcount(L);
	   if(L<3){
		.print("Victim cound is: ",L);	
	   	.print("Path list is: ",X);
	   	.length(X,Y);
	  	.nth(0,X,Z);//get fist position from path list
	   	.print("Scout next target is:",Z);
	   	.nth(0,X,pos(A,B));
	   	.send(scout,achieve,next_victim(A,B));//send new target to scout
	   	?path([_|E]);//remove first position from path list
	   	-+path(E);//restore path list
	  }
	.
/*
 * Initial plan for localization process.
 * Send command to scout to initial the localization process.
 * If possibilities remain is more than 1 then move to next position.
 * If possibilities remain is less or equal to 1 then localization process done.
 */	
 +!localizationinit
 	<-
 		.send(scout,achieve,localize);
 		.wait(3000);
 		?possibilitiescount(P);
 		if(P > 1){
 			!next_position_localization
 		}else{
 			.send(scout,achieve,localizationdone);
 		}
 	.
 
/*
 * If possibilities remain is less or equal to 1 then the localization process is done.
 */ 	
+!next_position_localization: possibilitiescount(Y) & Y<=1
	<-
		.send(scout,achieve,localizationdone);
		.wait(3000);
	.

/*
 * If possibilities remain is more than 1 then move to next position.
 */	
 +!next_position_localization: possibilitiescount(P) & P >1
 	<-
 		.send(scout,achieve,next_position_localization);
 		-possibilitiescount(_);
 	.
