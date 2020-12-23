package edu.ucsb.cs.cs184.kstubbsgeopics.post

class Post(s: String, s1: String, s2:  String, d: Double, d1: Double, s4: String) {
    var postUID: String = s
    var userUID: String = s1
    var photoPath: String = s2
    var latitude  = d
    var longitude = d1
    var caption: String = s4
}