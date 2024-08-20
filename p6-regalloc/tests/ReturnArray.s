	.text
ReturnArray.foo:
	pushq %rbp
	movq %rsp,%rbp
	subq $64,%rsp
	movq %rbx, -32(%rbp)
	movq %rdi,16(%rbp)
L.6:
	movq $3,%rbx
	xorq %rax,%rax
	cmpq %rbx,%rax
	jge L.2
L.1:
	movq $0,-24(%rbp,%rax,8)
	movq %rax,t.7
	addq $1,t.7
	movq t.7,%rax
	cmpq %rbx,%rax
	jl L.1
L.2:
	xorq %rax,%rax
	movq $2,%rcx
	movq $1,%rbx
L.3:
	movq %rax,ReturnArray.foo.1.1.i
	movq ReturnArray.foo.1.1.i,-24(%rbp,ReturnArray.foo.1.1.i,8)
	movq %rax,t.8
	addq %rbx,t.8
	movq t.8,%rax
L.4:
	cmpq %rcx,%rax
	jle L.3
L.5:
	movq 16(%rbp),%rbx
	movq %rbx,t.5
	movq t.5,%rdi
	movq %rbp,%rax
	addq $-24,%rax
	movq %rax,%rsi
	movq $24,%rdx
	call memmove
	movq %rax,t.6
	movq t.5,%rax
L.0:
#	returnSink
	movq -32(%rbp),%rbx
	addq $64,%rsp
	popq %rbp
	ret
ReturnArray.foo.badSub:
	call badSub
