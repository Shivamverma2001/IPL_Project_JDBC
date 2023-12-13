package com.io.mountblue.ipl;

import java.sql.*;
import java.util.*;

public class Main {

    public static final String DELIVERY_FILE_PATH=
            "/media/shivam/HP/Documents/Problem_solving/SV/Java-Full-Stack/iplprojectsv/Data/matches.csv";
    public static final String MATCH_FILE_PATH=
            "/media/shivam/HP/Documents/Problem_solving/SV/Java-Full-Stack/iplprojectsv/Data/matches.csv";
    public static final int MATCH_ID=1;
    public static final int MATCH_SEASON=2;
    public static final int MATCH_CITY=3;
    public static final int MATCH_DATE=4;
    public static final int MATCH_TEAM1=5;
    public static final int MATCH_TEAM2=6;
    public static final int MATCH_TOSS_WINNER=7;
    public static final int MATCH_TOSS_DECISION=8;
    public static final int MATCH_WINNER=11;
    public static final int MATCH_WIN_BY_RUNS=12;
    public static final int MATCH_VENUE=15;
    public static final int DELIVERY_ID=1;
    public static final int DELIVERY_INNING=2;
    public static final int DELIVERY_BATTING_TEAM=3;
    public static final int DELIVERY_BALLING_TEAM=4;
    public static final int DELIVERY_OVER=5;
    public static final int DELIVERY_BALL=6;
    public static final int DELIVERY_BATSMAN=7;
    public static final int DELIVERY_BOWLER_NAME=8;
    public static final int DELIVERY_WIDE_RUNS=11;
    public static final int DELIVERY_BYE_RUNS=12;
    public static final int DELIVERY_LEGBYE_RUNS=13;
    public static final int DELIVERY_NOBALL_RUNS=14;
    public static final int DELIVERY_PENALTY_RUNS=15;
    public static final int DELIVERY_EXTRA_RUNS=17;
    public static final int DELIVERY_TOTAL_RUNS=18;

    public static Connection con=null;

    public static void main(String[] args) throws SQLException {
        connectJDBC();

        List<Match> matches=getMatchesData();
        List<Delivery> deliveries=getDeliveriesData();

          findNumberOfMatchesPlayedPerYear(matches);
        findNumberOfMatchesWonOfAllTeamsOverAllYears(matches);
        findExtraRunsConcededPerTeamIn2016(matches,deliveries);
        findTheMostEconomicalBowlerIn2016(matches,deliveries);
        findTheTeamsWhoWinBothTossAndMatch(matches);

        con.close();
    }

    private static void connectJDBC() {
        String url="jdbc:postgresql://localhost:5432/postgres";
        String username="postgres";
        String password="8055";

        try {
            con = DriverManager.getConnection(url, username, password);
        }catch(SQLException e){
            e.printStackTrace();
        }
    }

    public static String[] customSplit(String data) {
        ArrayList<String> columns = new ArrayList<>();
        StringBuilder currentColumn = new StringBuilder();
        boolean inQuotes = false;

        for (char c : data.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                columns.add(currentColumn.toString().replace("\"", ""));
                currentColumn.setLength(0);
            } else {
                currentColumn.append(c);
            }
        }

        columns.add(currentColumn.toString().replace("\"", ""));

        return columns.toArray(new String[0]);
    }

    private static void findTheTeamsWhoWinBothTossAndMatch(List<Match> matches) {
        HashMap<String,Integer> numberOfteamsWinBothTossAndMatch=new HashMap<>();

        for(Match match:matches){
            String tossWinnerTeam=match.getTossWinner();
            String matchWinnerTeam=match.getWinner();

            if(tossWinnerTeam.equals(matchWinnerTeam)) {
                int numberOfMatchesPlayedYearly = numberOfteamsWinBothTossAndMatch
                        .getOrDefault(tossWinnerTeam, 0) + 1;
                numberOfteamsWinBothTossAndMatch.put(tossWinnerTeam, numberOfMatchesPlayedYearly);
            }
        }

        for(Map.Entry<String,Integer> teamWinTossAndMatch:numberOfteamsWinBothTossAndMatch.entrySet()){
            System.out.println("Teams: "+teamWinTossAndMatch.getKey());
            System.out.println("Number of Matches Won: "+teamWinTossAndMatch.getValue());
            System.out.println();
        }
    }

    private static void findTheMostEconomicalBowlerIn2016(List<Match> matches, List<Delivery> deliveries) {
        HashMap<String, Integer> bowlerTotalBalls = new HashMap<>();
        HashMap<String, Integer> bowlerTotalRuns = new HashMap<>();
        HashSet<String> matchIdFor2015 = new HashSet<>();

        for (Match match : matches) {
            String matchDate = match.getSeason();

            if (matchDate.contains("2015")) {
                matchIdFor2015.add(match.getId());
            }
        }

        for (Delivery delivery : deliveries) {
            String deliveryMatchId = delivery.getId();
            String bowlerName = delivery.getBowlerName();
            int noBallsRuns = Integer.parseInt(delivery.getNoballRuns());
            int wideRuns = Integer.parseInt(delivery.getWideRuns());
            int byeRuns = Integer.parseInt(delivery.getByeRuns());
            int legByeRuns = Integer.parseInt(delivery.getLegbyeRuns());
            int penaltyRuns = Integer.parseInt(delivery.getPenaltyRuns());
            int totalRunsPerBall = Integer.parseInt(delivery.getTotalRuns());

            if (matchIdFor2015.contains(deliveryMatchId)) {
                int matchRunsConceded =totalRunsPerBall - legByeRuns - byeRuns - penaltyRuns;

                int totalRunsConceded = bowlerTotalRuns.getOrDefault(bowlerName, 0);
                totalRunsConceded += matchRunsConceded;
                bowlerTotalRuns.put(bowlerName, totalRunsConceded);

                int totalBallsBowled = bowlerTotalBalls.getOrDefault(bowlerName, 0);

                if(wideRuns==0 || noBallsRuns==0){
                    totalBallsBowled++;
                }
                bowlerTotalBalls.put(bowlerName, totalBallsBowled);
            }
        }

        Map<String, Double> economyRate = new HashMap<>();
        for (Map.Entry<String, Integer> entry : bowlerTotalRuns.entrySet()) {
            String bowler = entry.getKey();
            int runs = entry.getValue();
            int balls = bowlerTotalBalls.get(bowler);
            double economy = (double) (runs*6.0) / balls;
            economyRate.put(bowler, economy);
        }

        List<Map.Entry<String, Double>> entries = new ArrayList<>(economyRate.entrySet());
        entries.sort(Comparator.comparing(Map.Entry::getValue));

        System.out.println("Most economical bowler in 2015");
        for (Map.Entry<String, Double> bowlerWithRuns: entries) {
            String bowler=bowlerWithRuns.getKey();
            double economy=bowlerWithRuns.getValue();
            if(economy!=0)
            System.out.println(bowler + " " + economy);
        }

    }

    private static void findExtraRunsConcededPerTeamIn2016(List<Match> matches, List<Delivery> deliveries) {
        HashMap<String,Integer> extraRunsConcededPerTeam=new HashMap<>();
        HashSet<String> matchIdFor2016=new HashSet<>();

        for(Match match:matches){
            String matchDate=match.getSeason();

            if(matchDate.contains("2016")){
                matchIdFor2016.add(match.getId());
            }
        }

        for(Delivery delivery:deliveries){
            String deliveryMatchId=delivery.getId();
            String bowlingTeam=delivery.getBallingTeam();
            int extraRuns=Integer.parseInt(delivery.getExtraRuns());

            if(matchIdFor2016.contains(deliveryMatchId)){
                extraRunsConcededPerTeam.
                        put(bowlingTeam,extraRunsConcededPerTeam.getOrDefault(bowlingTeam,0)+extraRuns);
            }
        }

        System.out.println("Extra runs conceded per team in 2016");
        for(Map.Entry<String,Integer> extraRunPerYear:extraRunsConcededPerTeam.entrySet()){
            System.out.println("Year: "+extraRunPerYear.getKey()+" "+" Matches: "+extraRunPerYear.getValue());
        }
    }

    private static void findNumberOfMatchesWonOfAllTeamsOverAllYears(List<Match> matches) {
        HashMap<String,Integer> matchesWonByTeam=new HashMap<>();

        for(Match match:matches){
            String winnerTeam=match.getWinner();

            if(winnerTeam!="") {
                int numberOfMatchesWonByTeamYearly = matchesWonByTeam.getOrDefault(winnerTeam, 0) + 1;
                matchesWonByTeam.put(match.getWinner(), numberOfMatchesWonByTeamYearly);
            }
        }

        for(Map.Entry<String,Integer> matchesPerYear:matchesWonByTeam.entrySet()){
            System.out.println("Team: "+matchesPerYear.getKey());
            System.out.println("Matches Won by that team: "+matchesPerYear.getValue());
            System.out.println();
        }
    }

    private static void findNumberOfMatchesPlayedPerYear(List<Match> matches) {
        HashMap<String,Integer> matchesPlayedPerYear=new HashMap<>();

        for(Match match:matches){
            int numberOfMatchesPlayedYearly=matchesPlayedPerYear.getOrDefault(match.getSeason(),0)+1;
            matchesPlayedPerYear.put(match.getSeason(),numberOfMatchesPlayedYearly);
        }

        System.out.println("Number of Matches Played per Year");
        for(Map.Entry<String,Integer> matchesPerYear:matchesPlayedPerYear.entrySet()){
            System.out.println("Year: "+matchesPerYear.getKey()+" "+" Matches: "+matchesPerYear.getValue());
        }
    }

    private static List<Delivery> getDeliveriesData() {
        List<Delivery> deliveries=new ArrayList<>();

        Statement st=null;
        String sql="Select * from deliveries";

        try{
            st=con.createStatement();
            ResultSet rs=st.executeQuery(sql);

            while(rs.next()){

                Delivery delivery=new Delivery();
                delivery.setId(rs.getString(DELIVERY_ID));
                delivery.setInning(rs.getString(DELIVERY_INNING));
                delivery.setBattingTeam(rs.getString(DELIVERY_BATTING_TEAM));
                delivery.setBallingTeam(rs.getString(DELIVERY_BALLING_TEAM));
                delivery.setOver(rs.getString(DELIVERY_OVER));
                delivery.setBall(rs.getString(DELIVERY_BALL));
                delivery.setBatsman(rs.getString(DELIVERY_BATSMAN));
                delivery.setBowlerName(rs.getString(DELIVERY_BOWLER_NAME));
                delivery.setWideRuns(rs.getString(DELIVERY_WIDE_RUNS));
                delivery.setByeRuns(rs.getString(DELIVERY_BYE_RUNS));
                delivery.setLegbyeRuns(rs.getString(DELIVERY_LEGBYE_RUNS));
                delivery.setNoballRuns(rs.getString(DELIVERY_NOBALL_RUNS));
                delivery.setPenaltyRuns(rs.getString(DELIVERY_PENALTY_RUNS));
                delivery.setExtraRuns(rs.getString(DELIVERY_EXTRA_RUNS));
                delivery.setTotalRuns(rs.getString(DELIVERY_TOTAL_RUNS));

                deliveries.add(delivery);
            }
            rs.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return deliveries;
    }

    private static List<Match> getMatchesData() {
        List<Match> matches=new ArrayList<>();

        Statement st=null;
        String sql="Select * from matches";

        try{
            st=con.createStatement();
            ResultSet rs=st.executeQuery(sql);

            while(rs.next()){

                Match match=new Match();
                match.setId(rs.getString(MATCH_ID));
                match.setSeason(rs.getString(MATCH_SEASON));
                match.setCity(rs.getString(MATCH_CITY));
                match.setDate(rs.getString(MATCH_DATE));
                match.setTeam1(rs.getString(MATCH_TEAM1));
                match.setTeam2(rs.getString(MATCH_TEAM2));
                match.setTossWinner(rs.getString(MATCH_TOSS_WINNER));
                match.setTossDecision(rs.getString(MATCH_TOSS_DECISION));
                match.setWinner(rs.getString(MATCH_WINNER));
                match.setWinByRuns(rs.getString(MATCH_WIN_BY_RUNS));
                match.setVenue(rs.getString(MATCH_VENUE));

                matches.add(match);
            }

            rs.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return matches;
    }
}