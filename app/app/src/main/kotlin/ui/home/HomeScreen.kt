package com.example.appcantina.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.appcantina.data.local.NewsEntity
import com.example.appcantina.data.model.AuthState
import com.example.appcantina.ui.components.InfoCard
import com.example.appcantina.ui.components.SectionTitle
import com.example.appcantina.util.Formatters

@Composable
fun HomeScreen(
    authState: AuthState,
    news: List<NewsEntity>,
    onSaveNews: (String, String) -> Unit,
    onDeleteNews: (NewsEntity) -> Unit,
    onOpenMenu: () -> Unit,
    onOpenForm: () -> Unit,
    onOpenHistory: () -> Unit,
    onOpenAdmin: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = "Inicio",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (authState.isInstitutional) "Aluno UDESC" else "Usuario externo",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item {
            LegalNotice()
        }
        item {
            SectionTitle("Noticias")
        }
        if (authState.isAdmin) {
            item {
                NewsEditor(onSaveNews = onSaveNews)
            }
        }
        if (news.isEmpty()) {
            item {
                InfoCard {
                    Text(
                        text = "Nenhum aviso publicado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        items(news, key = { it.id }) { item ->
            NewsCard(
                news = item,
                canDelete = authState.isAdmin,
                onDelete = { onDeleteNews(item) }
            )
        }
        item {
            HomeActionCard(
                title = "Cardapio",
                subtitle = "Almoco, janta e itens extras",
                icon = Icons.Default.List,
                onClick = onOpenMenu
            )
        }
        item {
            HomeActionCard(
                title = "Formulario",
                subtitle = "Reservar almoco",
                icon = Icons.Default.Edit,
                onClick = onOpenForm
            )
        }
        item {
            HomeActionCard(
                title = "Historico",
                subtitle = "Pedidos anteriores e status",
                icon = Icons.Default.History,
                onClick = onOpenHistory
            )
        }
        if (authState.isAdmin) {
            item {
                HomeActionCard(
                    title = "Admin",
                    subtitle = "Editar cardapio e itens",
                    icon = Icons.Default.Settings,
                    onClick = onOpenAdmin
                )
            }
        }
    }
}

@Composable
private fun LegalNotice() {
    InfoCard {
        Text(
            text = "Aviso juridico",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Este aplicativo serve apenas para reserva de almoco e consulta dos itens disponiveis. O pagamento deve ser efetuado diretamente no caixa.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun NewsEditor(
    onSaveNews: (String, String) -> Unit
) {
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }

    InfoCard {
        Text(
            text = "Publicar aviso",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Titulo") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Informacao importante") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                onSaveNews(title, body)
                title = ""
                body = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Publicar noticia")
        }
    }
}

@Composable
private fun NewsCard(
    news: NewsEntity,
    canDelete: Boolean,
    onDelete: () -> Unit
) {
    InfoCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = news.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = Formatters.dateTimeLabel(news.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (canDelete) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Remover aviso")
                }
            }
        }
        Text(
            text = news.body,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun HomeActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Button(onClick = onClick) {
                Text("Abrir")
            }
        }
    }
}
