	.text
ReturnRecord.foo:
	pushq %rbp
	movq %rsp,%rbp
	subq $64,%rsp
	movq %rbx, -32(%rbp)
	movq %rdi,16(%rbp)
L.1:
	xorq %rax,%rax
	movq %rax,-24(%rbp)
	xorq %rax,%rax
	movq %rax,-16(%rbp)
	xorq %rax,%rax
	movq %rax,-8(%rbp)
	movq $1,%rax
	movq %rax,-24(%rbp)
	movq $2,%rax
	movq %rax,-16(%rbp)
	movq $3,%rax
	movq %rax,-8(%rbp)
	movq 16(%rbp),%rax
	movq %rax,%rbx
	movq %rbx,%rdi
	movq %rbp,%rax
	addq $-24,%rax
	movq %rax,%rsi
	movq $24,%rax
	movq %rax,%rdx
	call memcpy
#	movq %rax,%rax
	movq %rbx,%rax
L.0:
#	returnSink
	movq -32(%rbp),%rbx
	addq $64,%rsp
	popq %rbp
	ret
ReturnRecord.foo.badSub:
	call badSub
	.text
ReturnRecord:
	pushq %rbp
	movq %rsp,%rbp
	subq $80,%rsp
	movq %rbx, -56(%rbp)
L.2:
	movq %rbp,%rax
	addq $-48,%rax
	movq %rax,%rdi
	call ReturnRecord.foo
	movq %rax,%rbx
	movq %rbp,%rax
	addq $-24,%rax
	movq %rax,%rdi
	movq %rbx,%rsi
	movq $24,%rax
	movq %rax,%rdx
	call memcpy
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
ReturnRecord.badSub:
	call badSub
