package com.example.employee;

import jakarta.persistence.*;

@Entity
@Table(name = "employees")
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long empid;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "years_of_experience", nullable = false)
    private Integer yearsOfExperience;

    @Column(nullable = false, length = 100)
    private String city;

    protected Employee() {}

    public Employee(String name, Integer yearsOfExperience, String city) {
        update(name, yearsOfExperience, city);
    }

    public void update(String name, Integer yearsOfExperience, String city) {
        this.name = name;
        this.yearsOfExperience = yearsOfExperience;
        this.city = city;
    }

    public Long getEmpid() { return empid; }
    public String getName() { return name; }
    public Integer getYearsOfExperience() { return yearsOfExperience; }
    public String getCity() { return city; }
}
