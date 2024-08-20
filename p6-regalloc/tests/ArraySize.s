	.data
	.balign 8
ArraySize.x:
	.zero 8
	.data
	.balign 8
ArraySize.y:
	.zero 8
	.text
ArraySize.println:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %rdi,%rbx
L.25:
	xorq %rax,%rax
	cmpq %rax,%rbx
	jl L.1
L.2:
	xorq %rax,%rax
	cmpq %rax,%rbx
	je L.5
L.6:
	movq %rbp,%r10
	movq %rbx,%rdi
	call ArraySize.println.1.f
	movq %rax,t.31
L.7:
	movq $10,%rdi
	call putchar
	movq %rax,t.32
	jmp L.0
L.1:
	movq $45,%rdi
	call putchar
	movq %rax,t.29
	xorq %rax,%rax
	movq %rax,t.36
	subq %rbx,t.36
	movq t.36,%rbx
	jmp L.2
L.5:
	movq $48,%rdi
	call putchar
	movq %rax,t.30
	jmp L.7
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
ArraySize.println.badSub:
	call badSub
