package com.example.pawfectmatchapp.models

data class DogData(
    var id: String,//id mutable dynamically
    val name: String,
    val age: Int,
    val breed: String,
    val gender: String,
    val description: String,
    val imageUrl: String
) {
    // Builder Class for Creating a Dog Object
    class Builder {
        private var id: String = ""
        private var name: String = ""
        private var age: Int = 0
        private var breed: String = ""
        private var gender: String = ""
        private var description: String = ""
        private var imageUrl: String = ""

        fun setId(id: String) = apply{this.id = id}
        fun setName(name: String) = apply { this.name = name }
        fun setAge(age: Int) = apply { this.age = age }
        fun setBreed(breed: String) = apply { this.breed = breed }
        fun setGender(gender: String) = apply { this.gender = gender }
        fun setDescription(description: String) = apply { this.description = description }
        fun setImageUrl(imageUrl: String) = apply { this.imageUrl = imageUrl }

        fun build() = DogData(id, name, age, breed, gender, description, imageUrl)
    }
}
