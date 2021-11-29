package com.wiryadev.firestore_compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
                        onRetrieveClicked = {
                            retrievePersons()
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

    private fun retrievePersons() = lifecycleScope.launch(Dispatchers.IO) {
        try {
            val querySnapshot = personCollectionRef.get().await()
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
}

@Composable
fun Form(
    persons: String,
    onSaveClicked: (Person) -> Unit,
    onRetrieveClicked: () -> Unit,
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
                try {
                    onSaveClicked(
                        Person(firstName.trim(), lastName.trim(), age.trim().toInt())
                    )
                } catch (e: Exception) {
                    Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
                }

            }
        ) {
            Text(text = "Save Data")
        }

        Button(
            onClick = {
                try {
                    onRetrieveClicked()
                } catch (e: Exception) {
                    Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show()
                }
            }
        ) {
            Text(text = "Retrieve Data")
        }

        Text(text = persons)
    }
}