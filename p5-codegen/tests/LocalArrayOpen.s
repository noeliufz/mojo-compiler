	.text
LocalArrayOpen:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.4:
	movq $10,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	jl LocalArrayOpen.badSub
L.0:
#	movq %rbx,%rbx
	movq $16,%rax
#	movq %rax,%rax
	movq %rbx,%rcx
	imulq $8,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
#	movq %rax,%rax
	movq %rax,%rdi
	call malloc
	movq %rax,%rdi
	movq %rdi,%rax
	addq $16,%rax
	movq %rax,0(%rdi)
	movq %rbx,8(%rdi)
	movq 8(%rdi),%rax
	movq %rax,%rsi
	movq 0(%rdi),%rax
	movq %rax,%rdx
	xorq %rax,%rax
	movq %rax,%rcx
	cmpq %rsi,%rcx
	jge L.3
L.1:
	movq %rcx,%rax
	imulq $8,%rax
	movq %rdx,%rbx
	addq %rax,%rbx
	xorq %rax,%rax
	movq %rax,0(%rbx)
	movq %rcx,%rax
	addq $1,%rax
	movq %rax,%rcx
L.2:
	cmpq %rsi,%rcx
	jl L.1
L.3:
	movq %rdi,%rax
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
LocalArrayOpen.badSub:
	call badSub
