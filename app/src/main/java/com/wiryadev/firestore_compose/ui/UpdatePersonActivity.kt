package com.wiryadev.firestore_compose.ui

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.SetOptions
import com.wiryadev.firestore_compose.MainActivity
import com.wiryadev.firestore_compose.Person
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UpdatePersonActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val person = intent.getParcelableExtra<Person>("extra_person") as Person

        setContent {
            UpdateForm(
                person = person,
                onUpdateClicked = { firstName, lastName, age ->
                    updatePerson(
                        person = person,
                        newPerson = getNewPerson(firstName, lastName, age)
                    )
                },
                onDeleteClicked = {
                    deletePerson(person)
                    finish()
                }
            )
        }
    }

    private fun getNewPerson(firstName: String, lastName: String, age: String): Map<String, Any> {
        val map = mutableMapOf<String, Any>()
        if (firstName.isNotEmpty()) {
            map["firstName"] = firstName
        }

        if (lastName.isNotEmpty()) {
            map["lastName"] = lastName
        }

        if (age.isNotEmpty()) {
            if (age.toIntOrNull() != null) {
                map["age"] = age.toInt()
            }
        }
        return map
    }

    private fun updatePerson(person: Person, newPerson: Map<String, Any>) =
        lifecycleScope.launch(Dispatchers.IO) {
            val personQuery = MainActivity.personCollectionRef
                .whereEqualTo("firstName", person.firstName)
                .whereEqualTo("lastName", person.lastName)
                .whereEqualTo("age", person.age)
                .get()
                .await()

            if (personQuery.documents.isNotEmpty()) {
                for (document in personQuery) {
                    try {
                        MainActivity.personCollectionRef.document(document.id)
                            .set(newPerson, SetOptions.merge())
                            .await()
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@UpdatePersonActivity, e.message, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@UpdatePersonActivity,
                        "No person matches the query",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

    private fun deletePerson(person: Person) = lifecycleScope.launch(Dispatchers.IO) {
            val personQuery = MainActivity.personCollectionRef
                .whereEqualTo("firstName", person.firstName)
                .whereEqualTo("lastName", person.lastName)
                .whereEqualTo("age", person.age)
                .get()
                .await()

            if (personQuery.documents.isNotEmpty()) {
                for (document in personQuery) {
                    try {
                        // Delete single document
                        MainActivity.personCollectionRef.document(document.id)
                            .delete()
                            .await()

                        // Delete only single field, for example delete firstName field
//                        MainActivity.personCollectionRef.document(document.id)
//                            .update(
//                                mapOf("firstName" to FieldValue.delete())
//                            )
                    } catch (e: Exception) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@UpdatePersonActivity, e.message, Toast.LENGTH_LONG)
                                .show()
                        }
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@UpdatePersonActivity,
                        "No person matches the query",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

}

@Composable
fun UpdateForm(
    person: Person,
    onUpdateClicked: (String, String, String) -> Unit,
    onDeleteClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                vertical = 36.dp,
                horizontal = 24.dp,
            ),
        verticalArrangement = Arrangement.spacedBy(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        var newFirstName by remember { mutableStateOf("") }
        var newLastName by remember { mutableStateOf("") }
        var newAge by remember { mutableStateOf("") }

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = person.firstName,
                modifier = Modifier.weight(1f)
            )
            TextField(
                value = newFirstName,
                onValueChange = {
                    newFirstName = it
                },
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = person.lastName,
                modifier = Modifier.weight(1f)
            )
            TextField(
                value = newLastName,
                onValueChange = {
                    newLastName = it
                },
                modifier = Modifier.weight(1f),
            )
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Text(
                text = "${person.age}",
                modifier = Modifier.weight(1f)
            )
            TextField(
                value = newAge,
                onValueChange = {
                    newAge = it
                },
                modifier = Modifier.weight(1f),
            )
        }

        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Button(
                onClick = {
                    onUpdateClicked(
                        newFirstName, newLastName, newAge
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Update Data")
            }
            Button(
                onClick = onDeleteClicked,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Delete Data")
            }
        }
    }
}