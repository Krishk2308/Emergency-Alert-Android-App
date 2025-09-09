package com.example.safetyalarmapp

class User {
    var name: String? = null
    var email: String? = null
    var phone: String? = null
    var bloodGroup: String? = null
    var gender: String? = null
    var uid: String? = null
    // Emergency contact details
    var eName1: String? = null
    var eEmail1: String? = null
    var ePhone1: String? = null
    var eName2: String? = null
    var eEmail2: String? = null
    var ePhone2: String? = null
    var eName3: String? = null
    var eEmail3: String? = null
    var ePhone3: String? = null

    constructor() {}

    constructor(name: String?, email: String?, phone: String?, bloodGroup: String?, gender: String?, uid: String?) {
        this.name = name
        this.email = email
        this.phone = phone
        this.bloodGroup = bloodGroup
        this.gender = gender
        this.uid = uid
    }

    constructor(
        eName1: String?, ePhone1: String?, eEmail1: String?,
        eName2: String?, ePhone2: String?, eEmail2: String?,
        eName3: String?, ePhone3: String?, eEmail3: String?, uid: String?
    ) {
        this.eName1 = eName1
        this.eEmail1 = eEmail1
        this.ePhone1 = ePhone1
        this.eName2 = eName2
        this.eEmail2 = eEmail2
        this.ePhone2 = ePhone2
        this.eName3 = eName3
        this.eEmail3 = eEmail3
        this.ePhone3 = ePhone3
        this.uid = uid
    }
}