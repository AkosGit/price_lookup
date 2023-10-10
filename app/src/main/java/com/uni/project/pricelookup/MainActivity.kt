package com.uni.project.pricelookup

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import com.example.compose.PriceLookupTheme
import com.example.compose.md_theme_dark_primaryContainer
import com.uni.project.pricelookup.Views.*
import com.uni.project.pricelookup.components.CameraCapture
import com.uni.project.pricelookup.components.SearchWidget
import java.io.File


class MainActivity : ComponentActivity() {
    companion object{
        fun CleanUpImages(context: Context){
            val preferencesManager = PreferencesManager(context)
            preferencesManager.saveData("photoLoc", "")
            preferencesManager.saveData("product","")
            val jpgFiles = File(preferencesManager.getData("outputDir","")).listFiles { file -> file.name.endsWith(".jpg") }
            jpgFiles?.forEach { file -> file.delete() }

        }
    }

    //camera permisson stuff
    private var shouldShowCamera: MutableState<Boolean> = mutableStateOf(false)
    val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Log.i("kilo", "Permission granted")
            shouldShowCamera.value = true
        } else {
            Log.i("kilo", "Permission denied")
        }
    }
    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.i("kilo", "Permission previously granted")
                shouldShowCamera.value = true
            }

            ActivityCompat.shouldShowRequestPermissionRationale(
                this,
                Manifest.permission.CAMERA
            ) -> Log.i("kilo", "Show camera permissions dialog")

            else -> requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    private fun getOutputDirectory(): File {
        val mediaDir = externalMediaDirs.firstOrNull()?.let {
            File(it, resources.getString(R.string.app_name)).apply { mkdirs() }
        }

        return if (mediaDir != null && mediaDir.exists()) mediaDir else filesDir
    }

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestCameraPermission()
        setContent {
            PriceLookupTheme {
                //saving output dir for saving files
                val context=LocalContext.current
                CleanUpImages(context)
                val preferencesManager = remember { PreferencesManager(context) }
                preferencesManager.saveData("outputDir",getOutputDirectory().absolutePath)

                val navController = rememberNavController()
                Surface (
                    color = MaterialTheme.colorScheme.background,
                    content = {
                        Column {
                            TopAppBar(
                                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                ),
                                title = {
                                    Text("Price checker")
                                },
                                navigationIcon = {
                                    IconButton(onClick = {
                                        // TODO: will need to implement: openNavDrawer()
                                    }) {
                                        Icon(
                                            Icons.Rounded.Menu,
                                            contentDescription = "Localized description",
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(
                                        onClick = {
                                            navController.navigate("BarcodeCameraView")
                                        },
//                                        shape = RoundedCornerShape(size = 20.dp),
                                        content = {
                                            Icon(
                                                Icons.Rounded.CameraAlt,
                                                contentDescription = "Localized description",
                                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                                            )
                                        }
                                    )
                                }
                            )

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                        ,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                        ),
                        shape = RectangleShape,
                        content = {
                            val searchText= remember {
                                mutableStateOf("")
                            }
                            SearchWidget(
                                text = searchText.value,
                                onTextChange = {
                                    searchText.value=it
                                    ;
                                },
                                onSearchClicked = {
                                    navController.navigate("SearchScreen/{query}".replace(
                                        oldValue = "{query}",
                                        newValue = it
                                    ))
                                },
                                onCloseClicked = {
                                    searchText.value=""
                                }
                            )

                                }
                            )

                            //basically that's the router :)
                            NavHost(navController = navController, startDestination = "MainPage") {
                                composable("MainPage") {
                                    MainPage(navigation = navController)

                                }
                                composable("SearchScreen/{query}") {backStackEntry ->
                                    SearchScreen(navigation = navController,backStackEntry.arguments?.getString("query"))
                                }
                                composable("ItemEditScreen") {
                                    ItemEditScreen(navigation = navController)
                                }

                                composable("ItemDetailsScreen/{itemId}") {backStackEntry ->
                                    ItemDetailsScreen(navigation = navController,backStackEntry.arguments?.getString("itemId"))
                                }
                                composable("BarcodeCameraView") {backStackEntry ->
                                    BarcodeCameraView(navigation = navController)
                                }
                                composable("ProductCameraView") {backStackEntry ->
                                    ProductCameraView(navigation = navController)
                                }
                            }
                        }
                    }
                )

                }

            }
        }
    }