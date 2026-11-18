package com.esteban.notekeeperapp

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.esteban.notekeeperapp.ui.theme.NoteKeeperAppTheme
import org.json.JSONArray
import org.json.JSONObject

/**
 * MainActivity es el punto de entrada principal de la aplicación Note Keeper.
 * Configura el diseño Edge-to-Edge e inicia el controlador de navegación.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            NoteKeeperAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigator()
                }
            }
        }
    }
}

/**
 * Estructura de datos que representa una nota individual con título y contenido.
 */
data class NoteModel(
    val title: String,
    val content: String
)

/**
 * Controlador de navegación principal que alterna entre la pantalla
 * de gestión de notas ("home") y la pantalla de información ("about").
 */
@Composable
fun AppNavigator() {
    var currentScreen by remember { mutableStateOf("home") }

    when (currentScreen) {
        "home" -> NoteKeeperScreen(
            onNavigateToAbout = { currentScreen = "about" }
        )
        "about" -> AboutScreen(
            onNavigateBack = { currentScreen = "home" }
        )
    }
}

/**
 * Pantalla principal de la aplicación con campos separados para Título y Contenido,
 * permitiendo crear, listar con título personalizado, editar y eliminar con confirmación.
 *
 * @param onNavigateToAbout Callback para navegar hacia la pantalla About.
 */
@Composable
fun NoteKeeperScreen(onNavigateToAbout: () -> Unit) {
    val context = LocalContext.current

    // Estados para los campos de entrada de título y contenido
    var titleInput by remember { mutableStateOf("") }
    var contentInput by remember { mutableStateOf("") }

    // Lista de notas cargadas en memoria
    var notesList by remember { mutableStateOf(listOf<NoteModel>()) }

    // Controla si el listado inferior es visible
    var isListVisible by remember { mutableStateOf(false) }

    // Índice de la nota en edición (-1 si es una nota nueva)
    var editingIndex by remember { mutableIntStateOf(-1) }

    // Índice de la nota pendiente de confirmación para eliminar
    var noteIndexToDelete by remember { mutableIntStateOf(-1) }

    // Mensaje de estado para retroalimentación visual
    var statusMessage by remember { mutableStateOf("") }

    // Diálogo modal de confirmación antes de borrar una nota
    if (noteIndexToDelete != -1) {
        val noteToDeleteObj = notesList.getOrNull(noteIndexToDelete)
        AlertDialog(
            onDismissRequest = { noteIndexToDelete = -1 },
            title = {
                Text(text = "Delete Note")
            },
            text = {
                Text(text = "Are you sure you want to delete '${noteToDeleteObj?.title ?: "this note"}'? This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val index = noteIndexToDelete
                        if (index in notesList.indices) {
                            val updatedList = notesList.toMutableList()
                            updatedList.removeAt(index)
                            saveNotesListToStorage(context, updatedList)
                            notesList = updatedList

                            if (editingIndex == index) {
                                editingIndex = -1
                                titleInput = ""
                                contentInput = ""
                            } else if (editingIndex > index) {
                                editingIndex -= 1
                            }

                            statusMessage = "Note deleted successfully."
                        }
                        noteIndexToDelete = -1
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        noteIndexToDelete = -1
                        statusMessage = "Deletion cancelled."
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp)
    ) {
        Text(
            text = "Note Keeper App",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (editingIndex == -1) "Create a new note below" else "Editing note: '${titleInput}'",
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Campo de texto para el Título de la nota
        OutlinedTextField(
            value = titleInput,
            onValueChange = { titleInput = it },
            label = { Text("Note Title") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Campo de texto para el Contenido de la nota
        OutlinedTextField(
            value = contentInput,
            onValueChange = { contentInput = it },
            label = { Text("Note Content") },
            modifier = Modifier
                .fillMaxWidth()
                .height(100.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Fila de botones principales
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    val trimmedTitle = titleInput.trim()
                    val trimmedContent = contentInput.trim()

                    if (trimmedTitle.isNotEmpty() && trimmedContent.isNotEmpty()) {
                        val currentStoredNotes = loadNotesListFromStorage(context).toMutableList()
                        val newNote = NoteModel(trimmedTitle, trimmedContent)

                        if (editingIndex == -1) {
                            currentStoredNotes.add(newNote)
                            statusMessage = "Note saved!"
                        } else {
                            if (editingIndex in currentStoredNotes.indices) {
                                currentStoredNotes[editingIndex] = newNote
                                statusMessage = "Note updated!"
                            }
                            editingIndex = -1
                        }

                        saveNotesListToStorage(context, currentStoredNotes)
                        titleInput = ""
                        contentInput = ""

                        if (isListVisible) {
                            notesList = currentStoredNotes
                        }
                    } else {
                        statusMessage = "Please fill both title and content."
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text(if (editingIndex == -1) "Save Note" else "Update Note")
            }

            Button(
                onClick = {
                    notesList = loadNotesListFromStorage(context)
                    isListVisible = true
                    statusMessage = if (notesList.isEmpty()) {
                        "No saved notes found."
                    } else {
                        "Loaded ${notesList.size} note(s)."
                    }
                },
                modifier = Modifier.weight(1f)
            ) {
                Text("Load Notes")
            }
        }

        if (editingIndex != -1) {
            Spacer(modifier = Modifier.height(4.dp))
            TextButton(
                onClick = {
                    editingIndex = -1
                    titleInput = ""
                    contentInput = ""
                    statusMessage = "Edit cancelled."
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Cancel Edit")
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        if (statusMessage.isNotEmpty()) {
            Text(
                text = statusMessage,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Saved Notes List",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (!isListVisible) {
            Text(
                text = "Press 'Load Notes' to view your saved notes here.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        } else if (notesList.isEmpty()) {
            Text(
                text = "Your notes list is currently empty.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(notesList) { index, noteItem ->
                    NoteItemCard(
                        note = noteItem,
                        onEditClick = {
                            titleInput = noteItem.title
                            contentInput = noteItem.content
                            editingIndex = index
                            statusMessage = "Editing '${noteItem.title}'..."
                        },
                        onDeleteClick = {
                            noteIndexToDelete = index
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        TextButton(
            onClick = onNavigateToAbout,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        ) {
            Text("About this App")
        }
    }
}

/**
 * Componente visual que representa una tarjeta de nota mostrando su Título personalizado
 * en lugar de un número genérico, junto con su contenido y botones de acción.
 */
@Composable
fun NoteItemCard(
    note: NoteModel,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Muestra el Título personalizado de la nota
            Text(
                text = note.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Muestra el contenido redactado
            Text(
                text = note.content,
                fontSize = 14.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                OutlinedButton(
                    onClick = onEditClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text("Edit", fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedButton(
                    onClick = onDeleteClick,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "Delete",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

/**
 * Segunda pantalla de la aplicación (About).
 */
@Composable
fun AboutScreen(onNavigateBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "About Note Keeper",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Note Keeper App is a multi-screen Android application built with Kotlin and Jetpack Compose for managing personal notes with custom titles.",
            fontSize = 16.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Key Features:\n" +
                    "• Create, view, edit, and delete notes with custom titles (CRUD).\n" +
                    "• Interactive delete confirmation dialogs.\n" +
                    "• Persistent local storage using JSON serialization in SharedPreferences.\n" +
                    "• Multi-screen navigation architecture.",
            fontSize = 15.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Developed by Esteban Spitale",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Back to Notes")
        }
    }
}

/**
 * Serializa la lista de objetos NoteModel a formato JSON para guardarla en SharedPreferences.
 */
fun saveNotesListToStorage(context: Context, notes: List<NoteModel>) {
    val sharedPreferences = context.getSharedPreferences("NotePrefs", Context.MODE_PRIVATE)
    val jsonArray = JSONArray()
    for (note in notes) {
        val jsonObj = JSONObject().apply {
            put("title", note.title)
            put("content", note.content)
        }
        jsonArray.put(jsonObj)
    }
    with(sharedPreferences.edit()) {
        putString("saved_notes_list_v2", jsonArray.toString())
        apply()
    }
}

/**
 * Lee y deserializa el JSON guardado en SharedPreferences convirtiéndolo en una lista de NoteModel.
 */
fun loadNotesListFromStorage(context: Context): List<NoteModel> {
    val sharedPreferences = context.getSharedPreferences("NotePrefs", Context.MODE_PRIVATE)
    val jsonString = sharedPreferences.getString("saved_notes_list_v2", null) ?: return emptyList()

    val notesList = mutableListOf<NoteModel>()
    val jsonArray = JSONArray(jsonString)
    for (i in 0 until jsonArray.length()) {
        val jsonObj = jsonArray.getJSONObject(i)
        val title = jsonObj.getString("title")
        val content = jsonObj.getString("content")
        notesList.add(NoteModel(title, content))
    }
    return notesList
}