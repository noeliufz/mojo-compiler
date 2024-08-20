	.text
ReturnArray.foo:
	pushq %rbp
	movq %rsp,%rbp
	subq $64,%rsp
	movq %rbx, -32(%rbp)
	movq %rdi,16(%rbp)
L.6:
	movq $3,%rax
	movq %rax,%rdx
	xorq %rax,%rax
	movq %rax,%rcx
	cmpq %rdx,%rcx
	jge L.2
L.1:
	movq %rcx,%rax
	imulq $8,%rax
	movq %rbp,%rbx
	addq %rax,%rbx
	xorq %rax,%rax
	movq %rax,-24(%rbx)
	movq %rcx,%rax
	addq $1,%rax
	movq %rax,%rcx
	cmpq %rdx,%rcx
	jl L.1
L.2:
	xorq %rax,%rax
	movq %rax,%rcx
	movq $2,%rax
	movq %rax,%rsi
	movq $1,%rax
	movq %rax,%rdx
L.3:
#	movq %rcx,%rcx
	movq %rcx,%rbx
	imulq $8,%rbx
	movq %rbp,%rax
	addq %rbx,%rax
	movq %rcx,-24(%rax)
	movq %rcx,%rax
	addq %rdx,%rax
	movq %rax,%rcx
L.4:
	cmpq %rsi,%rcx
	jle L.3
L.5:
	movq 16(%rbp),%rax
	movq %rax,%rbx
	movq %rbx,%rdi
	movq %rbp,%rax
	addq $-24,%rax
	movq %rax,%rsi
	movq $24,%rax
	movq %rax,%rdx
	call memmove
#	movq %rax,%rax
	movq %rbx,%rax
L.0:
#	returnSink
	movq -32(%rbp),%rbx
	addq $64,%rsp
	popq %rbp
	ret
ReturnArray.foo.badSub:
	call badSub
	.text
ReturnArray:
	pushq %rbp
	movq %rsp,%rbp
	subq $80,%rsp
	movq %rbx, -56(%rbp)
L.7:
	movq %rbp,%rax
	addq $-48,%rax
	movq %rax,%rdi
	call ReturnArray.foo
	movq %rax,%rbx
	movq %rbp,%rax
	addq $-24,%rax
	movq %rax,%rdi
	movq %rbx,%rsi
	movq $24,%rax
	movq %rax,%rdx
	call memmove
#	movq %rax,%rax
	movq $48,%rax
	movq -24(%rbp),%rbx
#	movq %rax,%rax
	addq %rbx,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq $10,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq $48,%rax
	movq -16(%rbp),%rbx
#	movq %rax,%rax
	addq %rbx,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq $10,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq $48,%rax
	movq -8(%rbp),%rbx
#	movq %rax,%rax
	addq %rbx,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq $10,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
#	returnSink
	movq -56(%rbp),%rbx
	addq $80,%rsp
	popq %rbp
	ret
ReturnArray.badSub:
	call badSub
