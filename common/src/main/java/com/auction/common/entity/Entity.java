package com.auction.common.entity;

import java.time.LocalDateTime;
import java.util.UUID;

public abstract class Entity {
    protected String id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;

    public Entity(){
        this.id= UUID.randomUUID().toString();
        this.createdAt= LocalDateTime.now();
        this.updatedAt= LocalDateTime.now();
    }

    public void updateTimestamp(){
        this.updatedAt= LocalDateTime.now();
    }

    public String getId(){
        return this.id;
    }
    public void setId(String id){
        this.id= id;
    }
    public LocalDateTime getCreatedAt(){
        return this.createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt= createdAt;
    }
    public LocalDateTime getUpdatedAt(){
        return this.updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt= updatedAt;
    }
}
