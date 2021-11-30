package com.wiryadev.firestore_compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.wiryadev.firestore_compose.ui.theme.FirestorecomposeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val personCollectionRef = Firebase.firestore.collection("persons")
    private var persons = MutableLiveData("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        subscribeFirestore()

        setContent {
            FirestorecomposeTheme {
                var personsState by remember { mutableStateOf("") }
                Surface(color = MaterialTheme.colors.background) {
                    persons.observe(this, {
                        personsState = it
                    })

                    Form(
                        persons = personsState,
                        onSaveClicked = {
                            savePerson(it)
                        },
                        onRetrieveClicked = { minAge, maxAge ->
                            retrievePersons(minAge, maxAge)
                        }
                    )
                }
            }
        }
    }

    private fun savePerson(person: Person) = lifecycleScope.launch(Dispatchers.IO) {
        try {
            personCollectionRef.add(person).await()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, "Data Saved Successfully", Toast.LENGTH_LONG)
                    .show()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun retrievePersons(
        minAge: Int,
        maxAge: Int,
    ) = lifecycleScope.launch(Dispatchers.IO) {
        try {
            val querySnapshot = personCollectionRef
                .whereGreaterThan("age", minAge)
                .whereLessThan("age", maxAge)
                .orderBy("age")
                .get()
                .await()
            val sb = StringBuilder()
            for (document in querySnapshot.documents) {
                val person = document.toObject<Person>()
                sb.append("$person\n")
            }
            withContext(Dispatchers.Main) { persons.value = sb.toString() }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@MainActivity, e.message, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun subscribeFirestore() {
        personCollectionRef.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
            firebaseFirestoreException?.let {
                Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }

            querySnapshot?.let {
                val sb = StringBuilder()
                for (document in it) {
                    val person = document.toObject<Person>()
                    sb.append("$person\n")
                }
                persons.value = sb.toString()
            }
        }
    }
}

@Composable
fun Form(
    persons: String,
    onSaveClicked: (Person) -> Unit,
    onRetrieveClicked: (Int, Int) -> Unit,
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
        val context = LocalContext.current

        var firstName by remember {
            mutableStateOf("")
        }

        var lastName by remember {
            mutableStateOf("")
        }

        var age by remember {
            mutableStateOf("")
        }

        var minAge by remember {
            mutableStateOf("")
        }

        var maxAge by remember {
            mutableStateOf("")
        }

        TextField(
            value = firstName,
            onValueChange = {
                firstName = it
            }
        )

        TextField(
            value = lastName,
            onValueChange = {
                lastName = it
            }
        )

        TextField(
            value = age,
            onValueChange = {
                age = it
            }
        )

        Button(
            onClick = {
                age.toIntOrNull()?.let {
                    onSaveClicked(
                        Person(firstName.trim(), lastName.trim(), it)
                    )
                } ?: Toast.makeText(context, "Masukkan angka valid", Toast.LENGTH_LONG).show()
            }
        ) {
            Text(text = "Save Data")
        }

        Button(
            onClick = {
                try {
                    onRetrieveClicked(
                        minAge.toIntOrNull() ?: 0,
                        maxAge.toIntOrNull() ?: 0
                    )
                } catch (e: Exception) {
                    Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
                }
            }
        ) {
            Text(text = "Retrieve Data")
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            TextField(
                value = minAge,
                onValueChange = {
                    minAge = it
                },
                modifier = Modifier.weight(1f)
            )

            TextField(
                value = maxAge,
                onValueChange = {
                    maxAge = it
                },
                modifier = Modifier.weight(1f)
            )
        }

        Text(text = persons)
    }
}