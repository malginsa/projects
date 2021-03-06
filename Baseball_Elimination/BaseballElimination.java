public class BaseballElimination {

	private final int number; // number of teams
	private final String[] name; // teams name
	private final int[] wins; // number of wins of each team
	private final int[] losses; // number of losses of each team
	private final int[] rem; // number of remaining games of each team
	private final int[][] game; // number of remaining games against each team
	private FordFulkerson ff;
	private Bag<String> elim; // subset of teams that eliminates
	private Queue<String> teams = new Queue<String>(); // crutch

	// create a baseball division from given filename in format specified below
	public BaseballElimination(String filename) {
		In in = new In(filename);
		number = in.readInt();
		name = new String[number];
		wins = new int[number];
		losses = new int[number];
		rem = new int[number];
		game = new int[number][number];
		for (int i = 0; i < number; i++) {
			name[i] = in.readString();
			wins[i] = in.readInt();
			losses[i] = in.readInt();
			rem[i] = in.readInt();
			for (int j = 0; j < number; j++)
				game[i][j] = in.readInt();
			String str = name[i]+' '+wins[i]+' '+losses[i]+' '+rem[i];
			for (int j = 0; j < number; j++)
				str = str+' '+game[i][j];
//			StdOut.println(str);
		}
	}

	private void checkTeamname(String team) {
		for (int i = 0; i < number; i++)
			if (name[i].equals(team)) return;
		throw new java.lang.IllegalArgumentException();
	}

	private int getIDteam(String team) {
		for (int i = 0; i < number; i++)
			if (name[i].equals(team)) return i;
		return number + 1;
	}

	// number of teams
	public int numberOfTeams() { return number; }

	// all teams
	public Iterable<String> teams() {
		// fill up teams
		for (int i = 0; i < number; i++)
			teams.enqueue(name[i]);
		return teams;
	}
	
	// number of wins for given team
	public int wins(String team) {
		checkTeamname(team);
		return wins[getIDteam(team)];
	}
	
	// number of losses for given team
	public int losses(String team) {
		checkTeamname(team);
		return losses[getIDteam(team)];
	}
	
	// number of remaining games for given team
	public int remaining(String team) {
		checkTeamname(team);
		return rem[getIDteam(team)];
	}
	
	// number of remaining games between team1 and team2
	public int against(String team1, String team2) {
		checkTeamname(team1);
		checkTeamname(team2);
		return game[getIDteam(team1)][getIDteam(team2)];
	}
	
	// is given team eliminated?
	public boolean isEliminated(String team) {

		elim = new Bag<String>();
		if (number < 2) return false;
		checkTeamname(team);
		int idteam = getIDteam(team); // id of checked team

		// checking trivial elimination
		for (int i = 0; i < number; i++) {
			if (idteam == i) continue;
			if (wins[idteam] + rem[idteam] < wins[i]) elim.add(name[i]);
		}
		if (!elim.isEmpty()) return true;

		// checking nontrivial elimination
		int teamoffset = number * number; // offset for team array
		int source = number * number + number + 1; // s-vert
		int target = number * number + number; // t-vert
		double avail = 0; // flow of last hope
		FlowNetwork flownet = new FlowNetwork(number * number + number + 2);
		for (int i = 0; i < number; i++)
			for (int j = 0; j < number; j++) {
				if ((i == idteam) || (j == idteam))
					continue;
				if (i >= j)
					continue;
				avail += game[i][j];
				flownet.addEdge(new FlowEdge(
					source, i * number + j, game[i][j]));
				flownet.addEdge(new FlowEdge(
					i * number + j, teamoffset + i, Double.POSITIVE_INFINITY));
				flownet.addEdge(new FlowEdge(
					i * number + j, teamoffset + j, Double.POSITIVE_INFINITY));
			}
		for (int i = 0; i < number; i++)
			if (i != idteam)
				flownet.addEdge(new FlowEdge(
					teamoffset + i, target, wins[idteam] + rem[idteam] - wins[i]));
		ff = new FordFulkerson(flownet, source, target);

		if (Math.abs(ff.value() - avail) > 0.001) {
			for (int i = 0; i < number; i++)
				if (ff.inCut(number * number + i))
					elim.add(name[i]);
			return true;
		}
		return false;
	}
	
	// subset R of teams that eliminates given team; null if not eliminated
	public Iterable<String> certificateOfElimination(String team) {
		checkTeamname(team);
//		if (elim == null) isEliminated(team);
		isEliminated(team);
		if (elim.isEmpty()) return null;
		return elim;
	}

//	public static void main(String[] args) {
//		BaseballElimination division = new BaseballElimination(args[0]);
//		for (String t : division.certificateOfElimination("Philadelphia"))
//			StdOut.print(t + " ");
//	}
	public static void main(String[] args) {
		BaseballElimination division = new BaseballElimination(args[0]);
		for (String team : division.teams()) {
			if (division.isEliminated(team)) {
				StdOut.print(team + " is eliminated by the subset R = { ");
				for (String t : division.certificateOfElimination(team)) {
					StdOut.print(t + " ");
				}
				StdOut.println("}");
			}
			else {
				StdOut.println(team + " is not eliminated");
			}
		}
	}

}
