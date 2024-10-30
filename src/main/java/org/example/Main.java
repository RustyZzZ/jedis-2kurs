package org.example;

import redis.clients.jedis.Jedis;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class Main {
  public static void main(String[] args) {

//    try(Jedis jedis = new Jedis()){
//      jedis.set("student:1:name", "Rostyslav" );
//      jedis.del("student:1:name");
//      jedis.get("student:1:name");
//
//      jedis.hset("student:1", Map.of("firstName", "Rostyslav"));
//      jedis.hget("student:1", "firstName");
//      jedis.hgetAll("student:1");
//
//    }



    try (var jedis = new Jedis()) {
      var student = new Student(1, "Rostyslav",
          "Diachuk",
          new Group(243, "243", "243"),
          LocalDate.now().minusYears(4));

      var groupDao = new GroupDao(jedis);
      var studentDao = new StudentDao(jedis, groupDao);

      var id = studentDao.create(student);
      var studentFromRedis = studentDao.getById(id);

      System.out.println("***!**!*!**!*!*!*!**!*!*!*!**!*!*");
      System.out.println("studentFromRedis = " + studentFromRedis);

    }
  }
}

record GroupDao(Jedis jedis) {
  public Integer create(Group group) {
    jedis.hset("group:" + group.id(), group.getHashMap());
    return group.id();
  }

  public Group getById(Integer id) {
    return new Group().fromHashMap(jedis.hgetAll("group:" + id));
  }
}


record StudentDao(Jedis jedis, GroupDao groupDao) {


  public Integer create(Student student) {
    groupDao.create(student.getGroup());
    jedis.hset("student:" + student.getId(), student.getHashMap());
    return student.getId();
  }

  public Student getById(Integer id) {
    var map = jedis.hgetAll("student:"+id);
    var student = new Student().fromHashMap(map);
    student.setGroup(groupDao.getById(Integer.parseInt(map.get("group"))));
    return student;
  }


}

interface Redisable<T> {
  Map<String, String> getHashMap();

  T fromHashMap(Map<String, String> map);

}

record Group(Integer id, String fullName, String curator) implements Redisable<Group> {

  public Group() {
    this(null, null, null);
  }

  @Override
  public Map<String, String> getHashMap() {
    return Map.of(
        "id", id.toString(),
        "fullName", fullName,
        "curator", curator
    );
  }

  @Override
  public Group fromHashMap(Map<String, String> map) {
    return new Group(
        Integer.parseInt(map.getOrDefault("id", "0")),
        map.getOrDefault("fullName", ""),
        map.getOrDefault("curator", "")
    );
  }
}

class Student implements Redisable<Student> {
  private Integer id;
  private String firstName;
  private String lastName;
  private Group group;
  private LocalDate startingDate;


  //<editor-fold desc="LOMBOK TOP">
  public Student(Integer id, String firstName, String lastName, Group group, LocalDate starting) {
    this.id = id;
    this.firstName = firstName;
    this.lastName = lastName;
    this.group = group;
    this.startingDate = starting;
  }

  public Student() {
  }

  public Integer getId() {
    return id;
  }

  public void setId(Integer id) {
    this.id = id;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public Group getGroup() {
    return group;
  }

  public void setGroup(Group group) {
    this.group = group;
  }

  public LocalDate getStarting() {
    return startingDate;
  }

  public void setStarting(LocalDate starting) {
    this.startingDate = starting;
  }

  @Override
  public String toString() {
    return "Student{" +
        "id=" + id +
        ", firstName='" + firstName + '\'' +
        ", lastName='" + lastName + '\'' +
        ", group=" + group +
        ", startingDate=" + startingDate +
        '}';
  }


  // </editor-fold>

  @Override
  public Map<String, String> getHashMap() {
    return Map.of("id", id.toString(),
        "firstName", firstName,
        "lastName", lastName,
        "group", group.id().toString(),
        "startDate", startingDate.format(DateTimeFormatter.BASIC_ISO_DATE));
  }

  @Override
  public Student fromHashMap(Map<String, String> map) {
    this.id = Integer.parseInt(map.getOrDefault("id", "0"));
    this.firstName = map.getOrDefault("firstName", "");
    this.lastName = map.getOrDefault("lastName", "");
    this.startingDate = LocalDate.parse(map.getOrDefault("startDate",
            LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)),
        DateTimeFormatter.BASIC_ISO_DATE);
    return this;
  }

}
