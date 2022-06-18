package com.incheck.api.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import java.io.Serializable;
import java.util.Date;

@Getter
@Setter
@ToString
@RequiredArgsConstructor
@MappedSuperclass
public abstract class AutoUpdatable implements Serializable {
  @Column(name = "date_created", nullable = false)
  private Date dateCreated = new Date();

  @Column(name = "date_modified", nullable = false)
  private Date dateModified;

  @Column(nullable = false)
  private int version = 1;

  @PrePersist
  protected void onCreate() {
    dateCreated = new Date();
    if (dateModified == null) {
      dateModified = dateCreated;
    }
  }

  @PreUpdate
  protected void onUpdate() {
    dateModified = new Date();
  }
}
