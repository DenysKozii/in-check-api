package com.incheck.api.entity;

import lombok.RequiredArgsConstructor;

import javax.persistence.MappedSuperclass;

@RequiredArgsConstructor
@MappedSuperclass
public abstract class BaseEntity extends AutoUpdatable {

}