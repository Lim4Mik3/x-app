package com.example.app.android.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

val mockPosts: List<FeedPost> = buildList {
    var id = 1

    add(FeedPost(
        id = id++,
        title = "Assalto na padaria",
        location = "Rua das Flores, Osasco",
        timeAgo = "2min",
        distanceMeters = 150,
        category = "Ocorrência",
        subcategory = "Crime",
        content = "Acabaram de assaltar a padaria da esquina. Dois indivíduos em uma moto preta. Polícia já foi acionada. Cuidado quem estiver na região!",
        signalCount = 47,
        commentCount = 12,
        shareCount = 23,
        viewCount = 892
    ))

    add(FeedPost(
        id = id++,
        title = "Semáforo apagado",
        location = "Av. dos Autonomistas",
        timeAgo = "5min",
        distanceMeters = 320,
        category = "Alerta",
        subcategory = "Trânsito",
        content = "O semáforo do cruzamento com a Rua Yolanda tá apagado desde ontem à noite. Já vi quase dois acidentes hoje de manhã. Alguém reportou pra prefeitura?",
        signalCount = 31,
        commentCount = 8,
        shareCount = 15,
        viewCount = 456
    ))

    add(FeedPost(
        id = id++,
        title = "Feira livre amanhã",
        location = "Rua da Estação, Centro",
        timeAgo = "12min",
        distanceMeters = 800,
        category = "Comunidade",
        content = "Amanhã tem feira livre com frutas super frescas e preço ótimo. Manga a R\$2,99 o kg e abacaxi a R\$4,50. Imperdível!",
        signalCount = 18,
        commentCount = 5,
        shareCount = 9,
        viewCount = 234
    ))

    add(FeedPost(
        id = id++,
        title = "Cachorro perdido - Golden Retriever",
        location = "Pq. dos Pinheiros",
        timeAgo = "25min",
        distanceMeters = 450,
        category = "Comunidade",
        subcategory = "Animais",
        content = "Meu cachorro fugiu hoje de manhã, é um golden retriever chamado Thor com coleira azul. Quem vir por favor me avisa! Ele é muito dócil.",
        mediaUrls = listOf("https://example.com/dog.jpg"),
        mediaType = MediaType.PHOTO,
        signalCount = 64,
        commentCount = 22,
        shareCount = 45,
        viewCount = 1_340
    ))

    add(FeedPost(
        id = id++,
        title = "Alagamento na parte baixa",
        location = "Rua Bela Vista",
        timeAgo = "32min",
        distanceMeters = 1_200,
        category = "Alerta",
        subcategory = "Clima",
        content = "Chuva forte causou alagamento na parte baixa do bairro. Evitem a Rua Bela Vista e arredores. Água já tá na altura do meio-fio.",
        signalCount = 89,
        commentCount = 34,
        shareCount = 67,
        viewCount = 2_100
    ))

    add(FeedPost(
        id = id++,
        title = "Nova academia no bairro",
        location = "Rua Augusta, 450",
        timeAgo = "45min",
        distanceMeters = 600,
        category = "Comércio",
        content = "Inaugurou uma academia nova aqui no bairro com preços bem acessíveis. Mensal por R\$79,90 e tem aula de spinning e funcional. Alguém já foi?",
        signalCount = 12,
        commentCount = 15,
        shareCount = 4,
        viewCount = 178
    ))

    add(FeedPost(
        id = id++,
        title = "Buraco na rua tapado!",
        location = "Rua das Flores",
        timeAgo = "1h",
        distanceMeters = 200,
        category = "Informação",
        content = "O buraco na Rua das Flores finalmente foi tapado! Depois de 6 meses reclamando na prefeitura. Agora dá pra passar sem desviar.",
        signalCount = 56,
        commentCount = 18,
        shareCount = 12,
        viewCount = 678
    ))

    add(FeedPost(
        id = id++,
        title = "Queda de energia no quarteirão",
        location = "Rua 7 de Setembro",
        timeAgo = "1h",
        distanceMeters = 350,
        category = "Alerta",
        subcategory = "Infraestrutura",
        content = "Acabou a luz aqui no quarteirão inteiro. Já liguei na Enel e disseram que previsão é 3h pra normalizar. Alguém mais tá sem energia?",
        signalCount = 38,
        commentCount = 27,
        shareCount = 8,
        viewCount = 523
    ))

    add(FeedPost(
        id = id++,
        title = "Show gratuito no parque",
        location = "Parque Municipal",
        timeAgo = "2h",
        distanceMeters = 1_500,
        category = "Comunidade",
        subcategory = "Evento",
        content = "Show gratuito no parque esse domingo! Banda local tocando MPB das 16h às 19h. Levem cadeira de praia e canga. Vai ter food truck também.",
        signalCount = 72,
        commentCount = 14,
        shareCount = 38,
        viewCount = 1_890
    ))

    add(FeedPost(
        id = id++,
        title = "Multas na Rua 7",
        location = "Rua 7 de Setembro",
        timeAgo = "2h",
        distanceMeters = 380,
        category = "Alerta",
        subcategory = "Trânsito",
        content = "Cuidado ao estacionar na Rua 7. Tão multando quem para em fila dupla sem dó. Vi pelo menos 5 carros sendo multados agora de manhã.",
        signalCount = 41,
        commentCount = 19,
        shareCount = 28,
        viewCount = 734
    ))

    add(FeedPost(
        id = id++,
        title = "Ônibus 174 mudou itinerário",
        location = "Terminal Central",
        timeAgo = "3h",
        distanceMeters = 900,
        category = "Informação",
        subcategory = "Transporte",
        content = "O ônibus 174 mudou o itinerário e agora passa pela rua de cima. O ponto antigo foi desativado. Novo ponto fica em frente ao mercado.",
        signalCount = 25,
        commentCount = 11,
        shareCount = 16,
        viewCount = 445
    ))

    add(FeedPost(
        id = id++,
        title = "Arrecadação de roupas",
        location = "Igreja São José",
        timeAgo = "3h",
        distanceMeters = 700,
        category = "Comunidade",
        subcategory = "Solidariedade",
        content = "Estamos fazendo arrecadação de roupas pro inverno. Aceitamos tudo em bom estado! Podem deixar na Igreja São José das 8h às 17h.",
        signalCount = 93,
        commentCount = 7,
        shareCount = 52,
        viewCount = 1_567
    ))

    add(FeedPost(
        id = id++,
        title = "Falta d'água amanhã",
        location = "Bairro Jardim das Acácias",
        timeAgo = "4h",
        distanceMeters = 500,
        category = "Alerta",
        subcategory = "Infraestrutura",
        content = "Falta d'água prevista pra amanhã das 8h às 14h na região do Jardim das Acácias. Guardem água! Manutenção programada da Sabesp.",
        signalCount = 67,
        commentCount = 31,
        shareCount = 44,
        viewCount = 2_340
    ))

    add(FeedPost(
        id = id++,
        title = "Pelada no sábado",
        location = "Campinho do Parque",
        timeAgo = "4h",
        distanceMeters = 1_100,
        category = "Comunidade",
        subcategory = "Esporte",
        content = "Quem aí joga pelada no sábado? Tô montando um time pro campinho do parque. Começa 9h da manhã. Traga água e caneleira!",
        signalCount = 15,
        commentCount = 20,
        shareCount = 3,
        viewCount = 189
    ))

    add(FeedPost(
        id = id++,
        title = "Promoção no mercado da esquina",
        location = "Mercado Bom Preço",
        timeAgo = "5h",
        distanceMeters = 250,
        category = "Comércio",
        content = "Mercado da esquina tá com promoção de frutas hoje. Manga a R\$2,99 o kg, banana a R\$3,50 o cacho. Corre que acaba rápido!",
        signalCount = 22,
        commentCount = 6,
        shareCount = 11,
        viewCount = 312
    ))
}

@Composable
fun Feed(
    posts: List<FeedPost>,
    listState: LazyListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues()
) {
    LazyColumn(
        state = listState,
        modifier = modifier,
        contentPadding = contentPadding
    ) {
        items(posts, key = { it.id }) { post ->
            FeedPostItem(post = post)
        }
    }
}
