package com.uni.project.pricelookup.Views

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.uni.project.pricelookup.PreferencesManager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import com.uni.project.pricelookup.components.BottomSheet_google
import com.uni.project.pricelookup.components.ShopDropDown_google
import eu.wewox.modalsheet.ExperimentalSheetApi
import kotlinx.coroutines.*
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.google.mlkit.vision.text.Text
import com.uni.project.pricelookup.HTTP
import com.uni.project.pricelookup.R
import com.uni.project.pricelookup.components.NetworkError
import com.uni.project.pricelookup.models.SearchResult
import kotlin.io.encoding.Base64

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterialApi::class, ExperimentalMaterial3Api::class,
    DelicateCoroutinesApi::class, ExperimentalSheetApi::class, ExperimentalFoundationApi::class
)
@Composable
//TODO: item upload indicator + jump to homepage
fun ItemEditScreen(navigation: NavController) {
    val isLoaded = remember {
        mutableStateOf(false)
    }
    val isNetworkError = remember {
        mutableStateOf(false)
    }
    val isFailed = remember {
        mutableStateOf(false)
    }
    val client=HTTP(LocalContext.current)
    val defaultBigCardPadding = PaddingValues(start = 10.dp, top = 5.dp, end = 10.dp, bottom = 5.dp)

    //setup ocr data edit
    var detectedName= remember {
        mutableStateOf("")
    }
    var detectedPrice= remember {
        mutableStateOf(0)
    }

    //getting barcode photo
    val context = LocalContext.current
    val preferencesManager = remember { PreferencesManager(context) }
    val photoBarCode=preferencesManager.getData("photoLoc","")


    val photoPlusSign=androidx.compose.material.icons.Icons.Rounded.CameraEnhance


    var photoProduct= remember {
        mutableStateOf("")
    }

    var photoMain= remember {
        mutableStateOf(photoBarCode)
    }

    Column(
        Modifier.onFocusChanged {
            val photoLocProduct = preferencesManager.getData("product", "")
            if (photoLocProduct != "") {
                photoProduct.value = photoLocProduct

            }
        }
    ) {
        val smallPhotoModifier= Modifier
            .background(color = MaterialTheme.colorScheme.primaryContainer)
            .width(80.dp)
            .height(80.dp)
        val smallPhotoTextModifier= Modifier
            .width(150.dp)
            .height(80.dp)

        //photo carousel
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .clip(
                    shape = RoundedCornerShape(
                        topStart = 20.dp,
                        topEnd = 20.dp
                    )
                )
                .height(300.dp)
                .padding(top = 15.dp)
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ){
            val bitmapList : MutableList<String> = mutableListOf(
                photoBarCode, photoProduct.value
            )
            val pagerState = rememberPagerState(
                pageCount = { bitmapList.size }
            )
            HorizontalPager(
                state = pagerState,
                key = null,
            ){
                    index ->
                Box(
                    contentAlignment = Alignment.BottomCenter,
                    modifier = Modifier
                        .fillMaxSize()
                ){
                    //ha nincs adat, jelenítsen meg egy default image-t
                    if (bitmapList[index] === ""){
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 10.dp, end = 10.dp)
                                .clip(
                                    shape = RoundedCornerShape(
                                        topStart = 20.dp,
                                        topEnd = 20.dp
                                    )
                                )
                                .background(MaterialTheme.colorScheme.surfaceContainerLowest)
                            ,
                        ){
                            Image(
                                painterResource(R.mipmap.ic_launcher_foreground),
//                            painterResource(R.drawable.chocolate_bar1),
                                contentDescription = "appIcon",
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(start = 10.dp, end = 10.dp)
                                    .clip(
                                        shape = RoundedCornerShape(
                                            topStart = 20.dp,
                                            topEnd = 20.dp
                                        )
                                    )
                                ,
                            )
                            Box(
                                contentAlignment = Alignment.TopCenter,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp, end = 20.dp)
                                    .alpha(20f)
                            ){
                                Row (
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.fillMaxWidth().padding(start = 10.dp, top = 8.dp)
                                ){
                                    Text(text = "Not photo from product")
                                    IconButton(
                                        onClick = { navigation.navigate("ProductCameraView") },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            Icons.Rounded.AddAPhoto,
                                            contentDescription = "Localized description",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(25.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                    else{
                        AsyncImage(
                            model = bitmapList[index],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(start = 10.dp, end = 10.dp)
                                .clip(
                                    shape = RoundedCornerShape(
                                        topStart = 20.dp,
                                        topEnd = 20.dp
                                    )
                                )
                            ,
                        )
                    }
                    Box(
                        contentAlignment = Alignment.BottomEnd,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp, end = 20.dp)
                            .alpha(20f)
                    ){
                        Text(text = "${pagerState.currentPage+1}/${bitmapList.count()}")
                    }
                }
            }
        }

        //shop selection
        val shop= remember {
            mutableStateOf("Spar")
        }
        Row(Modifier.padding(30.dp)) {
            ShopDropDown_google(shop)
        }

        fun OnOCRSucces(visionText: Text){
            Toast.makeText(context,visionText.text,Toast.LENGTH_LONG).show()
        }
        fun OnOCRFail(Error: Exception){
            Toast.makeText(context,Error.message,Toast.LENGTH_LONG).show()
        }

        var visible= remember { mutableStateOf(false) }


        //Buttons
        Box(
            contentAlignment = Alignment.BottomCenter,
            modifier = Modifier.fillMaxSize()
        ){
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxWidth().padding(bottom = 40.dp)
            ){
                ElevatedButton(
                    onClick = {
                        //val ocr=OCR()
                        //ocr.MakeOCR(photoBarCode,context,{text-> })
                        //HTTP().sendImage(photoBarCode)
                    },
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.inversePrimary,
                        disabledContentColor = MaterialTheme.colorScheme.inversePrimary
                    ),
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(20.dp))
                        .size(70.dp)
                    ,
                    content = {
//                        Text(text = "Process Barcode")
                        androidx.compose.material.Icon(
                            Icons.Rounded.DocumentScanner,
                            contentDescription = "AddTask Icon",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )
                ElevatedButton(
                    onClick = { visible.value = true },
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.inversePrimary,
                        disabledContentColor = MaterialTheme.colorScheme.inversePrimary
                    ),
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(20.dp))
                        .size(70.dp)
                    ,
                    content = {
//                        Text(text = "Edit data")
                        androidx.compose.material.Icon(
                            Icons.Rounded.EditAttributes,
                            contentDescription = "AddTask Icon",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )
                ElevatedButton(
                    onClick = {
                        CoroutineScope(Dispatchers.IO).launch {
                            client.updateProduct(detectedName.value,shop.value,detectedPrice.value,photoProduct.value, {
                                //onSuccess
                                isLoaded.value=true
                                this.cancel("Fuck you")
                            }, {
                                //onFailure
                                isFailed.value=true
                                this.cancel("Fuck you")
                            },{
                                //onNetworkError
                                isNetworkError.value=true
                                this.cancel("Fuck you")
                            }
                            )
                        }
                    },
                    colors = ButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.primary,
                        disabledContainerColor = MaterialTheme.colorScheme.inversePrimary,
                        disabledContentColor = MaterialTheme.colorScheme.inversePrimary
                    ),
                    modifier = Modifier
                        .clip(shape = RoundedCornerShape(20.dp))
                        .size(70.dp)
                    ,
                    content = {
                        androidx.compose.material.Icon(
                            Icons.Rounded.CloudUpload,
                            contentDescription = "AddTask Icon",
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                )
            }
        }

        BottomSheet_google(detectedName,detectedPrice,visible)
        if(isNetworkError.value){
            NetworkError()
        }
        if(isFailed.value){
            NetworkError()
        }
    }
}




