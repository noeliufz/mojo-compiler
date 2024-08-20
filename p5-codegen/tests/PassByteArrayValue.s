	.text
PassByteArrayValue.fun:
	pushq %rbp
	movq %rsp,%rbp
	subq $32,%rsp
	movq %r13, -24(%rbp)
	movq %r12, -16(%rbp)
	movq %rbx, -8(%rbp)
	movq %rdi,16(%rbp)
L.9:
	xorq %rax,%rax
	movq %rax,%r13
	movq $4,%rax
	movq %rax,%r12
	movq $1,%rax
	movq %rax,%rbx
L.1:
	movq %r13,%rcx
	movq %rbp,%rax
	addq %rcx,%rax
	movsbq 16(%rax),%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
	movq %r13,%rax
	addq %rbx,%rax
	movq %rax,%r13
L.2:
	cmpq %r12,%r13
	jle L.1
L.3:
	movq $10,%rax
	movq %rax,%rdi
	call putchar
#	movq %rax,%rax
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	movq -16(%rbp),%r12
	movq -24(%rbp),%r13
	addq $32,%rsp
	popq %rbp
	ret
PassByteArrayValue.fun.badSub:
	call badSub
	.text
PassByteArrayValue:
	pushq %rbp
	movq %rsp,%rbp
	subq $32,%rsp
	movq %rbx, -16(%rbp)
L.10:
	movq $5,%rax
	movq %rax,%rdx
	xorq %rax,%rax
	movq %rax,%rcx
	cmpq %rdx,%rcx
	jge L.5
L.4:
	movq %rbp,%rbx
	addq %rcx,%rbx
	xorq %rax,%rax
	movq %rax,-8(%rbx)
	movq %rcx,%rax
	addq $1,%rax
	movq %rax,%rcx
	cmpq %rdx,%rcx
	jl L.4
L.5:
	xorq %rax,%rax
	movq %rax,%rcx
	movq $4,%rax
	movq %rax,%rsi
	movq $1,%rax
	movq %rax,%rdx
L.6:
#	movq %rcx,%rcx
	movq %rbp,%rbx
	addq %rcx,%rbx
	movq $48,%rax
#	movq %rax,%rax
	addq %rcx,%rax
	movq %rax,-8(%rbx)
	movq %rcx,%rax
	addq %rdx,%rax
	movq %rax,%rcx
L.7:
	cmpq %rsi,%rcx
	jle L.6
L.8:
	movq -8(%rbp),%rax
	movq %rax,%rdi
	call PassByteArrayValue.fun
#	movq %rax,%rax
#	returnSink
	movq -16(%rbp),%rbx
	addq $32,%rsp
	popq %rbp
	ret
PassByteArrayValue.badSub:
	call badSub
