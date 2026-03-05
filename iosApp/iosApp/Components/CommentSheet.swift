import SwiftUI

struct CommentSheet: View {
    let postId: String
    var onDismiss: () -> Void

    @Environment(\.appColors) private var colors
    @State private var comments: [ApiComment] = []
    @State private var isLoading = true
    @State private var commentText = ""
    @State private var replyingTo: ApiComment?
    @State private var isSending = false

    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                Text("Comentários")
                    .font(.system(size: 17, weight: .semibold))
                    .foregroundColor(colors.textPrimary)
                Spacer()
                Button(action: onDismiss) {
                    Image(systemName: "xmark")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(colors.textSecondary)
                        .frame(width: 28, height: 28)
                }
                .buttonStyle(.plain)
            }
            .padding(.horizontal, 20)
            .padding(.vertical, 16)

            Rectangle().fill(colors.divider).frame(height: 0.5)

            // Content
            if isLoading {
                Spacer()
                ProgressView().tint(colors.accent)
                Spacer()
            } else if comments.isEmpty {
                Spacer()
                Text("Nenhum comentário ainda")
                    .font(.system(size: 14))
                    .foregroundColor(colors.textSecondary)
                Spacer()
            } else {
                ScrollView {
                    LazyVStack(spacing: 0) {
                        ForEach(comments) { comment in
                            CommentItemView(
                                comment: comment,
                                onReply: { replyingTo = $0 },
                                onVote: { id, type in vote(id, type) },
                                onDelete: { id in deleteComment(id) }
                            )
                        }
                    }
                }
            }

            // Reply indicator
            if let reply = replyingTo {
                HStack {
                    Text("Respondendo a \(reply.authorName)")
                        .font(.system(size: 12))
                        .foregroundColor(colors.accent)
                    Spacer()
                    Button { replyingTo = nil } label: {
                        Image(systemName: "xmark")
                            .font(.system(size: 10))
                            .foregroundColor(colors.textSecondary)
                    }
                    .buttonStyle(.plain)
                }
                .padding(.horizontal, 20)
                .padding(.vertical, 8)
                .background(colors.surface)
            }

            // Input
            Rectangle().fill(colors.divider).frame(height: 0.5)
            HStack(spacing: 8) {
                TextField("Adicionar comentário...", text: $commentText)
                    .font(.system(size: 14))
                    .foregroundColor(colors.textPrimary)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 10)
                    .background(colors.surface)
                    .clipShape(RoundedRectangle(cornerRadius: 20))

                Button(action: sendComment) {
                    if isSending {
                        ProgressView()
                            .tint(colors.accent)
                            .frame(width: 36, height: 36)
                    } else {
                        Image(systemName: "paperplane.fill")
                            .font(.system(size: 16))
                            .foregroundColor(commentText.isEmpty ? colors.textSecondary : colors.accent)
                            .frame(width: 36, height: 36)
                    }
                }
                .buttonStyle(.plain)
                .disabled(commentText.isEmpty || isSending)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 12)
        }
        .background(colors.background)
        .task { await loadComments() }
    }

    private func loadComments() async {
        isLoading = true
        do {
            comments = try await ApiClient.shared.getComments(postId: postId)
        } catch {}
        isLoading = false
    }

    private func sendComment() {
        guard !commentText.isEmpty, !isSending else { return }
        let text = commentText
        let parentId = replyingTo?.id
        commentText = ""
        replyingTo = nil
        isSending = true

        Task {
            do {
                _ = try await ApiClient.shared.addComment(
                    postId: postId,
                    text: text,
                    authorName: TokenManager.shared.displayName ?? "Anônimo",
                    parentId: parentId
                )
                await loadComments()
            } catch {}
            isSending = false
        }
    }

    private func vote(_ commentId: String, _ type: String) {
        Task {
            try? await ApiClient.shared.voteComment(commentId: commentId, voteType: type)
            await loadComments()
        }
    }

    private func deleteComment(_ commentId: String) {
        Task {
            try? await ApiClient.shared.deleteComment(commentId: commentId)
            await loadComments()
        }
    }
}

// MARK: - Comment Item

private struct CommentItemView: View {
    let comment: ApiComment
    var onReply: (ApiComment) -> Void
    var onVote: (String, String) -> Void
    var onDelete: (String) -> Void
    var isReply: Bool = false

    @Environment(\.appColors) private var colors

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            // Author + time
            HStack(spacing: 6) {
                Text(comment.authorName.isEmpty ? "Anônimo" : comment.authorName)
                    .font(.system(size: 13, weight: .semibold))
                    .foregroundColor(colors.textPrimary)
                Text("·").foregroundColor(colors.textSecondary)
                Text(comment.timeAgo)
                    .font(.system(size: 11))
                    .foregroundColor(colors.textSecondary)
            }

            Text(comment.text)
                .font(.system(size: 14))
                .foregroundColor(colors.textPrimary)
                .lineSpacing(2)

            // Actions
            HStack(spacing: 4) {
                Button { onVote(comment.id, "up") } label: {
                    HStack(spacing: 2) {
                        Image(systemName: "chevron.up")
                            .font(.system(size: 12))
                        if comment.upvotes > 0 {
                            Text("\(comment.upvotes)").font(.system(size: 11))
                        }
                    }
                    .foregroundColor(colors.textSecondary)
                    .padding(4)
                }
                .buttonStyle(.plain)

                Button { onVote(comment.id, "down") } label: {
                    HStack(spacing: 2) {
                        Image(systemName: "chevron.down")
                            .font(.system(size: 12))
                        if comment.downvotes > 0 {
                            Text("\(comment.downvotes)").font(.system(size: 11))
                        }
                    }
                    .foregroundColor(colors.textSecondary)
                    .padding(4)
                }
                .buttonStyle(.plain)

                if !isReply {
                    Button { onReply(comment) } label: {
                        Text("Responder")
                            .font(.system(size: 12, weight: .medium))
                            .foregroundColor(colors.textSecondary)
                            .padding(4)
                    }
                    .buttonStyle(.plain)
                }

                Spacer()

                Button { onDelete(comment.id) } label: {
                    Image(systemName: "trash")
                        .font(.system(size: 12))
                        .foregroundColor(colors.textSecondary.opacity(0.5))
                }
                .buttonStyle(.plain)
            }
            .padding(.top, 2)

            // Replies
            if !comment.replies.isEmpty {
                ForEach(comment.replies) { reply in
                    CommentItemView(
                        comment: reply,
                        onReply: onReply,
                        onVote: onVote,
                        onDelete: onDelete,
                        isReply: true
                    )
                    .padding(.leading, 20)
                }
            }

            if !isReply {
                Rectangle()
                    .fill(colors.divider.opacity(0.5))
                    .frame(height: 0.5)
                    .padding(.top, 8)
            }
        }
        .padding(.horizontal, isReply ? 0 : 20)
        .padding(.vertical, isReply ? 4 : 12)
    }
}
