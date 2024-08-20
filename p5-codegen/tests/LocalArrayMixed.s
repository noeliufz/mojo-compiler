	.text
LocalArrayMixed:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.6:
	movq $10,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	jl LocalArrayMixed.badSub
L.0:
#	movq %rbx,%rbx
	movq $16,%rax
#	movq %rax,%rax
	movq %rbx,%rcx
	imulq $40,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
#	movq %rax,%rax
	movq %rax,%rdi
	call malloc
#	movq %rax,%rax
	movq %rax,%rcx
	addq $16,%rcx
	movq %rcx,0(%rax)
	movq %rbx,8(%rax)
	movq 8(%rax),%rbx
	movq %rbx,%rcx
	movq 0(%rax),%rbx
#	movq %rbx,%rbx
	xorq %rdx,%rdx
	movq %rdx,%r9
	cmpq %rcx,%r9
	jge L.3
L.1:
	movq $5,%rdx
	movq %rdx,%r8
	xorq %rdx,%rdx
	movq %rdx,%rdi
	cmpq %r8,%rdi
	jge L.5
L.4:
	movq %r9,%rdx
	imulq $40,%rdx
	movq %rbx,%rsi
	addq %rdx,%rsi
	movq %rdi,%rdx
	imulq $8,%rdx
#	movq %rsi,%rsi
	addq %rdx,%rsi
	xorq %rdx,%rdx
	movq %rdx,0(%rsi)
	movq %rdi,%rdx
	addq $1,%rdx
	movq %rdx,%rdi
	cmpq %r8,%rdi
	jl L.4
L.5:
	movq %r9,%rdx
	addq $1,%rdx
	movq %rdx,%r9
L.2:
	cmpq %rcx,%r9
	jl L.1
L.3:
#	movq %rax,%rax
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
LocalArrayMixed.badSub:
	call badSub
