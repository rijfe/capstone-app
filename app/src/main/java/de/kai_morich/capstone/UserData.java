package de.kai_morich.capstone;

public class UserData {
    String name;
    String rank;

    public UserData(String name, String rank){
        this.rank = rank;
        this.name = name;
    }

    public String getName(){
        return name;
    }

    public String getRank(){
        return rank;
    }

    public void setName(String name){
        this.name = name;
    }

    public void setRank(String rank){
        this.name = rank;
    }
}
