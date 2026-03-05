import SwiftUI

struct FeedPost: Identifiable {
    let id: Int
    let authorName: String
    let authorInitials: String
    let timeAgo: String
    let content: String
}

let mockPosts: [FeedPost] = {
    let names: [(String, String)] = [
        ("Lucas Mendes", "LM"),
        ("Ana Beatriz", "AB"),
        ("Carlos Eduardo", "CE"),
        ("Mariana Costa", "MC"),
        ("Pedro Henrique", "PH"),
        ("Juliana Alves", "JA"),
        ("Rafael Santos", "RS"),
        ("Fernanda Lima", "FL"),
        ("Gabriel Oliveira", "GO"),
        ("Isabela Rocha", "IR"),
        ("Thiago Nascimento", "TN"),
        ("Camila Ferreira", "CF"),
        ("Bruno Azevedo", "BA"),
        ("Letícia Martins", "LM"),
        ("Diego Correia", "DC")
    ]
    let contents = [
        "Alguém mais percebeu que o trânsito na Raposo Tavares tá impossível hoje? Saí de casa às 7h e ainda tô parado.",
        "Acabei de descobrir uma padaria incrível aqui perto da estação. O pão de queijo é o melhor que já comi na vida!",
        "Quem tiver precisando de um eletricista bom e barato, me chama no privado que indico o cara que fez aqui em casa.",
        "Vizinhança tá muito barulhenta ultimamente. Obra das 7h às 22h todo dia, não aguento mais. Alguém sabe se tem limite de horário?",
        "Feira livre amanhã na Rua da Estação. Frutas super frescas e o preço tá ótimo essa semana!",
        "Cuidado com o cruzamento da Av. dos Autonomistas com a Yolanda. Semáforo tá apagado desde ontem.",
        "Meu cachorro fugiu hoje de manhã, é um golden retriever chamado Thor. Quem vir por favor me avisa!",
        "Inaugurou uma academia nova aqui no bairro. Alguém já foi? Tô pensando em trocar da que eu vou.",
        "Dica: o mercado da esquina tá com promoção de frutas hoje. Manga a R$2,99 o kg!",
        "Alguém sabe o horário de funcionamento do novo posto de saúde? Preciso vacinar meu filho.",
        "Quem aí joga pelada no sábado? Tô montando um time pro campinho do parque.",
        "Vi um gato preto com coleira vermelha perdido perto da praça. Se alguém conhece o dono avisa!",
        "O buraco na Rua das Flores finalmente foi tapado! Depois de 6 meses reclamando na prefeitura.",
        "Recomendo demais o rodízio de pizza do lugar novo na Augusta. Preço justo e qualidade top.",
        "Tá caindo uma chuva forte aqui. Cuidado com alagamento na parte baixa do bairro!",
        "Acabou a luz aqui no quarteirão. Alguém mais tá sem energia?",
        "O ônibus 174 mudou o itinerário e agora passa pela rua de cima. Fiquem ligados!",
        "Farmácia da esquina tá fechando mais cedo agora, último horário é 21h.",
        "Quem quiser doar roupas, tô fazendo uma arrecadação pro inverno. Aceito tudo em bom estado!",
        "Show gratuito no parque esse domingo. Banda local tocando MPB das 16h às 19h.",
        "Alguém tem recomendação de dentista na região? Preciso de um que aceite convênio.",
        "O wifi do café da praça é surpreendentemente bom. Melhor lugar pra trabalhar remoto!",
        "Cuidado ao estacionar na Rua 7. Tão multando quem para em fila dupla sem dó.",
        "Minha vizinha faz bolo de pote maravilhoso. R$8 cada, sabores variados. Encomendas pelo zap!",
        "Criançada solta pipa no fio de luz de novo. Perigoso demais, alguém precisa falar com os pais.",
        "Achei uma carteira na calçada da Av. Brasil. Tem documentos dentro. Quem perdeu?",
        "O posto de gasolina da entrada do bairro tá com o preço mais barato da região. Aproveitei hoje.",
        "Inaugurou um pet shop novo com preços bem acessíveis. Banho e tosa por R$35.",
        "Falta d'água prevista pra amanhã das 8h às 14h. Guardem água!",
        "Alguém viu o pôr do sol hoje? Tava absurdo de bonito lá do mirante."
    ]
    return (0..<30).map { i in
        let (name, initials) = names[i % names.count]
        let time: String
        if i < 5 {
            time = "\(i + 1)min"
        } else if i < 15 {
            time = "\((i - 4) * 5)min"
        } else {
            time = "\(i / 5)h"
        }
        return FeedPost(
            id: i + 1,
            authorName: name,
            authorInitials: initials,
            timeAgo: time,
            content: contents[i]
        )
    }
}()

// MARK: - Feed

struct Feed: View {
    let posts: [FeedPost]
    var onScrollOffsetChanged: ((CGFloat) -> Void)? = nil
    var topInset: CGFloat = 0
    var bottomInset: CGFloat = 0

    var body: some View {
        ScrollView {
            LazyVStack(spacing: 0) {
                ForEach(posts) { post in
                    FeedPostItem(post: post)
                }
            }
            .padding(.top, topInset)
            .padding(.bottom, bottomInset)
            .background(
                ScrollViewObserver { offset in
                    onScrollOffsetChanged?(offset)
                }
            )
        }
    }
}

// MARK: - Feed Post Item

private struct FeedPostItem: View {
    let post: FeedPost

    @Environment(\.appColors) private var colors

    var body: some View {
        VStack(spacing: 0) {
            HStack(alignment: .top, spacing: 12) {
                ZStack {
                    Circle()
                        .fill(colors.avatarBackground)
                        .frame(width: 40, height: 40)
                    Text(post.authorInitials)
                        .font(.system(size: 13, weight: .semibold))
                        .foregroundColor(colors.textSecondary)
                }

                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 6) {
                        Text(post.authorName)
                            .font(.system(size: 14, weight: .semibold))
                            .foregroundColor(colors.textPrimary)
                        Text(post.timeAgo)
                            .font(.system(size: 12))
                            .foregroundColor(colors.textSecondary)
                    }
                    Text(post.content)
                        .font(.system(size: 15))
                        .foregroundColor(colors.textPrimary)
                        .lineSpacing(3)
                        .fixedSize(horizontal: false, vertical: true)
                }
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 14)

            Rectangle()
                .fill(colors.divider)
                .frame(height: 0.5)
                .padding(.leading, 72)
        }
    }
}

// MARK: - Preview

#Preview {
    Feed(posts: mockPosts)
}
