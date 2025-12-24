package com.example.iptvplayertv.presentation.livetv.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.tv.material3.Button
import androidx.tv.material3.ButtonDefaults
import androidx.tv.material3.ExperimentalTvMaterial3Api
import androidx.tv.material3.Text
import com.example.iptvplayertv.data.model.LiveCategory

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoryList(
    categories: List<LiveCategory>,
    selectedCategory: LiveCategory?,
    onCategorySelected: (LiveCategory) -> Unit
) {
    val listState = rememberLazyListState()

    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D)),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        contentPadding = PaddingValues(vertical = 20.dp, horizontal = 20.dp)
    ) {
        item {
            Text(
                text = "Categorías",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF5F5F5),
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        items(categories) { category ->
            CategoryItem(
                category = category,
                isSelected = category.categoryId == selectedCategory?.categoryId,
                onClick = { onCategorySelected(category) }
            )
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun CategoryItem(
    category: LiveCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    var isFocused by remember { mutableStateOf(false) }

    // Animación para el borde
    val borderWidth by animateDpAsState(
        targetValue = if (isFocused) 2.dp else 0.dp,
        animationSpec = tween(durationMillis = 200),
        label = "border_animation"
    )

    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(70.dp)
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
            }
            .border(
                width = borderWidth,
                color = if (isFocused) Color(0xFFD97706) else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            ),
        colors = ButtonDefaults.colors(
            containerColor = Color(0xFF1A1A1A),
            focusedContainerColor = Color(0xFF2A2A2A),
            pressedContainerColor = Color(0xFF2A2A2A),
            contentColor = Color(0xFFF5F5F5),
            focusedContentColor = Color(0xFFF5F5F5)
        ),
        shape = ButtonDefaults.shape(RoundedCornerShape(8.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.categoryName,
                fontSize = 18.sp,
                fontWeight = if (isFocused) FontWeight.Medium else FontWeight.Normal,
                color = Color(0xFFF5F5F5)
            )

            // Contador de canales
            Text(
                text = "${(10..30).random()}", // Reemplazar con category.channelCount
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                color = Color(0xFF888888)
            )
        }
    }
}

@Preview(
    name = "Television (4K)",
    device = "id:tv_4k",
    showBackground = true,
    backgroundColor = 0xFF0D0D0D
)
@Composable
fun CategoryListPreview() {
    val dummyCategories = listOf(
        LiveCategory(categoryId = "1", categoryName = "Deportes", parentId = 0),
        LiveCategory(categoryId = "2", categoryName = "Noticias", parentId = 0),
        LiveCategory(categoryId = "3", categoryName = "Películas", parentId = 0),
        LiveCategory(categoryId = "4", categoryName = "Infantiles", parentId = 0),
        LiveCategory(categoryId = "5", categoryName = "Música", parentId = 0),
        LiveCategory(categoryId = "6", categoryName = "Series", parentId = 0)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.35f)
                .fillMaxHeight()
                .background(Color(0xFF0D0D0D))
        ) {
            CategoryList(
                categories = dummyCategories,
                selectedCategory = dummyCategories[2],
                onCategorySelected = {}
            )
        }
    }
}