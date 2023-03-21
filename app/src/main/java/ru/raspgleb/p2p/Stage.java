package ru.raspgleb.p2p;
public class Stage {
    private String name; // Имя записи
    private String numeric; // Число

    Stage(){
        this.name = "no_name";
        this.numeric = "0";
    }

    Stage(String name,String numeric){
        this.name = name;
        this.numeric = numeric;
    }

    public String getName() {
        return name;
    }

    public String getNumeric() {
        return numeric;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNumeric(String numeric) {
        this.numeric = numeric;
    }
}
