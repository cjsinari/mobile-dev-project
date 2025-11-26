package com.example.marikitiapp.ui.product

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.*
import androidx.navigation.NavController
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.marikitiapp.ui.components.BottomNavigationBar
import com.example.marikitiapp.ui.theme.MarikitiGreen
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.io.InputStream


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostProductScreen(
    navController: NavController,
    onBack: () -> Unit ={},
    viewModelFactory: ProductViewModel.Factory = ProductViewModel.Factory()
) {
    val ctx = LocalContext.current
    val vm: ProductViewModel =
        androidx.lifecycle.viewmodel.compose.viewModel(factory = viewModelFactory)
    val isSaving by vm.isSaving.collectAsState()
    val saveSuccess by vm.saveSuccess.collectAsState()
    val error by vm.error.collectAsState()

    //form states
    var productName by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var quantityText by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var termsAccepted by remember { mutableStateOf(false) }

    //image states
    var imageBytes by remember { mutableStateOf<ByteArray?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val coroutineScope = rememberCoroutineScope()

    //gallery picker
    val pickImage =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                imageUri = it
                coroutineScope.launch {
                    imageBytes = readBytesFromUri(ctx.contentResolver, it)
                }
            }
        }

    //take picture preview (returns Bitmap)
    val takePhoto =
        rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bmp: Bitmap? ->
            bmp?.let {
                imageUri = null
                imageBytes = bitmapToByteArray(it)
            }
        }

    // categories
    val categories = listOf(
        "Accessories",
        "Fragrances",
        "Headphones & Earphones",
        "Jewellery",
        "Skincare",
        "Toys & Plushies"
    )
    var expanded by remember { mutableStateOf(false) }

    //dialogs
    var showConfirm by remember { mutableStateOf(false) }
    var showValidationError by remember { mutableStateOf<String?>(null) }

    //reset on success
    LaunchedEffect(saveSuccess) {
        saveSuccess?.let {
            if (it.isNotEmpty()) {
                showConfirm = true
            }
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Post a new product", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = { BottomNavigationBar(navController = navController) }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            //Image upload placeholder
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFEFEFEF))
                    .clickable { pickImage.launch("image/*.") },
                contentAlignment = Alignment.Center
            ) {
                when {
                    imageBytes != null -> {
                        val bmp = android.graphics.BitmapFactory.decodeByteArray(
                            imageBytes,
                            0,
                            imageBytes!!.size
                        )
                        bmp?.let {
                            Image(
                                bitmap = it.asImageBitmap(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    else -> Icon(
                        Icons.Default.CameraAlt,
                        contentDescription = null,
                        modifier = Modifier.size(36.dp),
                        tint = MarikitiGreen
                    )
                }
            }

            Spacer(Modifier.height(8.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                TextButton(onClick = { pickImage.launch("image/*") }) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = null);
                    Spacer(Modifier.width(8.dp));
                    Text("Gallery", color = MarikitiGreen)
                }
                TextButton(onClick = { takePhoto.launch(null) }) {
                    Icon(Icons.Default.CameraAlt, contentDescription = null);
                    Spacer(Modifier.width(8.dp));
                    Text("Camera", color = MarikitiGreen)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            //Product Name
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name") },
                placeholder = { Text("Enter product name") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            //Price
            OutlinedTextField(
                value = priceText,
                onValueChange = { priceText = it },
                label = { Text("Price") },
                placeholder = { Text("Enter price") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            //Product Description
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                placeholder = { Text("Enter product description") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            //Quantity
            OutlinedTextField(
                value = quantityText,
                onValueChange = { quantityText = it },
                label = { Text("Quantity") },
                placeholder = { Text("Enter quantity") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            //Category
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded }) {
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    readOnly = true,
                    label = { Text("Category") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )

                ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                    categories.forEach { sel ->
                        DropdownMenuItem(
                            text = { Text(sel) },
                            onClick = { category = sel; expanded = false })

                    }
                }
            }

            Spacer(Modifier.height(12.dp))

            //T's & C's
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MarikitiGreen,
                        uncheckedColor = Color.Gray
                    )
                )
                Spacer(Modifier.width(8.dp))
                Column {
                    Text("I accept the terms", fontSize = 14.sp)
                    Text(
                        "Read our T&Cs",
                        fontSize = 13.sp,
                        color = MarikitiGreen,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
            //Space for button

            Button(
                onClick = {
                    val price = priceText.toDoubleOrNull()
                    val qty = quantityText.toIntOrNull()
                    when {
                        productName.isBlank() -> showValidationError = "Product name is required"
                        price == null || price <= 0.0 -> showValidationError = "Enter a valid price"
                        qty == null || qty < 0 -> showValidationError = "Enter a valid quantity"
                        category.isBlank() -> showValidationError = "Please select a category"
                        imageBytes == null -> showValidationError = "Please add a product image"
                        !termsAccepted -> showValidationError =
                            "Please accept the Terms & Conditions"

                        else -> vm.uploadAndSave(
                            ctx,
                            productName.trim(),
                            price,
                            description.trim(),
                            qty,
                            category,
                            imageBytes!!
                        )
                    }
                },
                enabled = !isSaving,
                modifier = Modifier.fillMaxWidth().height(52.dp).padding(horizontal = 16.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MarikitiGreen,
                    contentColor = Color.White
                )
            ) {
                if (isSaving) CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = Color.White
                )
                else Text("Post product", fontSize = 18.sp)
            }

            //Validation dialog
            showValidationError?.let { msg ->
                AlertDialog(
                    onDismissRequest = { showValidationError = null },
                    title = { Text("Validation") },
                    text = { Text(msg) },
                    confirmButton = {
                        TextButton(onClick = { showValidationError = null }) { Text("OK") }
                    })
            }

            //Success dialog
            if (showConfirm) {
                SuccessScreen(navController)
            }

            //Error dialog
            error?.let { err ->
                AlertDialog(
                    onDismissRequest = {},
                    title = { Text("Error") },
                    text = { Text(err) },
                    confirmButton = {
                        TextButton(onClick = { /* maybe clear error in VM? */ }) { Text("OK") }
                    })

            }

        }

    }

}
//Helpers
fun bitmapToByteArray(bmp: Bitmap): ByteArray {
    val baos = ByteArrayOutputStream()
    bmp.compress(Bitmap.CompressFormat.JPEG, 85, baos)
    return baos.toByteArray()
}

fun readBytesFromUri(resolver: ContentResolver, uri: Uri): ByteArray? {
    val inputStream = resolver.openInputStream(uri)
    return inputStream?.readBytes() ?: ByteArray(0)
}

fun loadBitmapFromUri(context: Context, uri: Uri): android.graphics.Bitmap? {
    return try {
        if (Build.VERSION.SDK_INT >= 29) {
            val source =
                android.graphics.ImageDecoder.createSource(context.contentResolver, uri)
            android.graphics.ImageDecoder.decodeBitmap(source)
        } else {
            MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

