package com.auction.common.entity;

public abstract class User extends Entity {
   protected String username;
   protected String password;
   protected String email;
   protected String fullName;
   protected boolean active;

   public User(){
       super();
       this.active= true;
   }
   public abstract String getRole();

   public String getUsername(){
       return username;
   }
   public void setUsername(String username){
       this.username= username;
   }
   public boolean authenticate(String pwd){
       return this.password!= null && this.password.equals(pwd)&& this.active;
   }
   public String getPassword(){
       return this.password;
   }
   public void setPassword(String password){
       this.password= password;
   }
   public String getEmail(){
       return this. email;
   }
   public void setEmail(String email){
       this.email=email;
   }
   public boolean isActive(){
       return this.active;
   }
   public void setActive(boolean active){
       this.active= active;
   }

}
