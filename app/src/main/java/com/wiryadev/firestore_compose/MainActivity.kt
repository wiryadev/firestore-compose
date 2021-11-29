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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.wiryadev.firestore_compose.ui.theme.FirestorecomposeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    private val personCollectionRef = Firebase.firestore.collection("persons")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FirestorecomposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(color = MaterialTheme.colors.background) {
                    Form(
                        onSaveClicked = {
                            savePerson(it)
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
}

@Composable
fun Form(
    onSaveClicked: (Person) -> Unit,
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

    }
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    FirestorecomposeTheme {
        Form(onSaveClicked = {})
    }
}