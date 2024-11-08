package cn.edu.hitsz.compiler.asm;

public class Register {
    private final String name;
    private final int num;
    private boolean isUse;

    public Register(String name, int num){
        this.name = name;
        this.num = num;
        this.isUse = false;
    }

    public boolean isUse(){
        return isUse;
    }

    public void setUse(){
        this.isUse = true;
    }
    public void relUse(){
        this.isUse = false;
    }
    public int getNum(){
        return num;
    }
    public String getName(){
        return name;
    }
}
