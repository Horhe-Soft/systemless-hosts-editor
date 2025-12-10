package com.horhe.hostseditor

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.horhe.hostseditor.ui.theme.HostsEditorTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HostsEditorTheme {
                Column (
                    modifier = Modifier
                ) {
                    EditorScreen()
                }
            }
        }
    }
}

@Composable
fun EditorScreen(viewModel: FileViewModel = viewModel()) {
    LaunchedEffect(Unit) {
        if (viewModel.fileContent == null) {
            viewModel.readHostsFile()
        }
    }

    val context = LocalContext.current
    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        if (uri != null) {
            viewModel.exportToUri(context, uri)
        }
    }

    var isSaveButtonEnabled = true
    var isSaved = false
    var showSavedDialog by remember { mutableStateOf(false) }

    if (showSavedDialog == true) {
        SavedDialog(onConfirm = { showSavedDialog = false }, onDismiss = { showSavedDialog = false })
    }

    Scaffold {
        innerPadding ->
        Column (
            modifier = Modifier
            .padding(innerPadding)
            .fillMaxSize()
        ) {
            Row (
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                FuncButton(onClickExport = {
                    exportLauncher.launch("hosts_export.txt")
                },
                    onClickImport = {
                        Toast.makeText(context, "Currently not available", Toast.LENGTH_LONG).show()
                    }
                )
                Text("Systemless hosts edit", fontSize = 24.sp)
                Spacer(modifier = Modifier.weight(1f))
                SaveButton(onClick = { viewModel.saveHostsFile(context.cacheDir)
                    showSavedDialog = true }, isSaved = isSaved, isEnabled = isSaveButtonEnabled)
            }

            if (viewModel.fileContent == null) {
                Box(
                    modifier = Modifier.fillMaxSize().weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                TextField(
                    value = viewModel.fileContent!!,
                    onValueChange = { newText ->
                        viewModel.onContentChanged(newText)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .imePadding()
                        .weight(1f)
                )
            }
        }
    }
}

//---------BUTTONS----------

@Composable
fun SaveButton(onClick: () -> Unit,isSaved: Boolean, modifier: Modifier = Modifier, isEnabled: Boolean = true) {
    IconButton(
        onClick = onClick,
        enabled = isEnabled,
        modifier = modifier
    ) {
        if (isSaved == true) {
            Icon(
                imageVector = Icons.Filled.Done,
                contentDescription = "Already saved"
            )
        } else {
            Icon(
                painterResource(id = R.drawable.save_24px),
                contentDescription = "Save"
            )
        }
    }
}

@Composable
fun FuncButton(onClickExport: () -> Unit, onClickImport: () -> Unit, modifier: Modifier = Modifier) {
    var expanded by remember { mutableStateOf(false) }
    Box(
        modifier = modifier
            .padding(16.dp)
    ) {
        IconButton(onClick = { expanded = !expanded }) {
            Icon(Icons.Filled.Menu, contentDescription = "More options")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Export") },
                onClick = onClickExport
            )
            DropdownMenuItem(
                text = { Text("Import") },
                onClick = onClickImport
            )
        }
    }
}

//----------DIALOGS-----------

@Composable
fun SavedDialog(onDismiss: () -> Unit, onConfirm: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column(
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Successfully saved")
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Spacer(modifier = Modifier.weight(1f))
                Button(onClick = onConfirm) {
                    Text("Ok")
                }
                Spacer(modifier = Modifier.weight(1f))
            }
        }
    )
}

//----------PREVIEW-----------

@Preview
@Composable
fun EditorPreview() {
    EditorScreen()
}