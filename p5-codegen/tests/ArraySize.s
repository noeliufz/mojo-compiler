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
L.27:
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
#	movq %rax,%rax
L.7:
	movq $10,%rax
	movq %rax,%rdi
	call _putchar
#	movq %rax,%rax
	jmp L.0
L.1:
	movq $45,%rax
	movq %rax,%rdi
	call _putchar
#	movq %rax,%rax
	xorq %rax,%rax
#	movq %rax,%rax
	subq %rbx,%rax
	movq %rax,%rbx
	jmp L.2
L.5:
	movq $48,%rax
	movq %rax,%rdi
	call _putchar
#	movq %rax,%rax
	jmp L.7
L.0:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
ArraySize.println.badSub:
	call badSub
	.text
ArraySize.println.1.f:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
	movq %r10,%rcx
	movq %rdi,%rbx
L.28:
	xorq %rax,%rax
	cmpq %rax,%rbx
	jg L.9
L.10:
	jmp L.8
L.9:
	movq %rcx,%r10
	movq $10,%rcx
	movq %rbx,%rax
	cqto
	idivq %rcx
	movq %rax,%rcx
	movq %rcx,%rdi
	call ArraySize.println.1.f
#	movq %rax,%rax
	movq $10,%rcx
	movq %rbx,%rax
	cqto
	idivq %rcx
	movq %rax,%rcx
#	movq %rcx,%rcx
	imulq $10,%rcx
	movq %rbx,%rax
	subq %rcx,%rax
#	movq %rax,%rax
	addq $48,%rax
	movq %rax,%rdi
	call _putchar
#	movq %rax,%rax
	jmp L.10
L.8:
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
ArraySize.println.1.f.badSub:
	call badSub
	.text
ArraySize:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
	movq %rbx, -8(%rbp)
L.29:
	movq $5,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	jl ArraySize.badSub
L.11:
#	movq %rbx,%rbx
	movq $16,%rax
#	movq %rax,%rax
	movq %rbx,%rcx
	imulq $5,%rcx
#	movq %rax,%rax
	addq %rcx,%rax
#	movq %rax,%rax
	movq %rax,%rdi
	call _malloc
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
	jge L.14
L.12:
	movq $5,%rdx
	movq %rdx,%r8
	xorq %rdx,%rdx
	movq %rdx,%rdi
	cmpq %r8,%rdi
	jge L.16
L.15:
	movq %r9,%rsi
	imulq $5,%rsi
	movq %rbx,%rdx
	addq %rsi,%rdx
	movq %rdx,%rsi
	addq %rdi,%rsi
	xorq %rdx,%rdx
	movq %rdx,0(%rsi)
	movq %rdi,%rdx
	addq $1,%rdx
	movq %rdx,%rdi
	cmpq %r8,%rdi
	jl L.15
L.16:
	movq %r9,%rdx
	addq $1,%rdx
	movq %rdx,%r9
L.13:
	cmpq %rcx,%r9
	jl L.12
L.14:
	leaq ArraySize.x(%rip),%rbx
	movq %rax,0(%rbx)
	movq $5,%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	jl ArraySize.badSub
L.17:
#	movq %rbx,%rbx
	movq $16,%rax
#	movq %rax,%rax
#	movq %rax,%rax
	addq %rbx,%rax
#	movq %rax,%rax
	movq %rax,%rdi
	call _malloc
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
	jge L.20
L.18:
	movq %rdx,%rbx
	addq %rcx,%rbx
	xorq %rax,%rax
	movq %rax,0(%rbx)
	movq %rcx,%rax
	addq $1,%rax
	movq %rax,%rcx
L.19:
	cmpq %rsi,%rcx
	jl L.18
L.20:
	leaq ArraySize.y(%rip),%rax
	movq %rdi,0(%rax)
	leaq ArraySize.x(%rip),%rax
	movq 0(%rax),%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je ArraySize.badPtr
L.21:
	movq %rbx,%rax
	movq 8(%rax),%rax
#	movq %rax,%rax
#	movq %rax,%rax
	imulq $40,%rax
#	movq %rax,%rax
	movq %rax,%rdi
	call ArraySize.println
#	movq %rax,%rax
	leaq ArraySize.x(%rip),%rax
	movq 0(%rax),%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je ArraySize.badPtr
L.22:
	movq %rbx,%rax
	movq 8(%rax),%rax
#	movq %rax,%rax
#	movq %rax,%rax
	imulq $5,%rax
#	movq %rax,%rax
	movq %rax,%rdi
	call ArraySize.println
#	movq %rax,%rax
	movq $40,%rax
	movq %rax,%rdi
	call ArraySize.println
#	movq %rax,%rax
	movq $5,%rax
	movq %rax,%rdi
	call ArraySize.println
#	movq %rax,%rax
	leaq ArraySize.y(%rip),%rax
	movq 0(%rax),%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je ArraySize.badPtr
L.23:
	movq %rbx,%rax
	movq 8(%rax),%rax
#	movq %rax,%rax
#	movq %rax,%rax
	imulq $8,%rax
#	movq %rax,%rax
	movq %rax,%rdi
	call ArraySize.println
#	movq %rax,%rax
	leaq ArraySize.y(%rip),%rax
	movq 0(%rax),%rax
	movq %rax,%rbx
	xorq %rax,%rax
	cmpq %rax,%rbx
	je ArraySize.badPtr
L.24:
	movq %rbx,%rax
	movq 8(%rax),%rax
#	movq %rax,%rax
#	movq %rax,%rax
	movq %rax,%rdi
	call ArraySize.println
#	movq %rax,%rax
	movq $3,%rax
	movq %rax,%rdi
	call ArraySize.println
#	movq %rax,%rax
	movq $1,%rax
	movq %rax,%rdi
	call ArraySize.println
#	movq %rax,%rax
#	returnSink
	movq -8(%rbp),%rbx
	addq $16,%rsp
	popq %rbp
	ret
ArraySize.badPtr:
	call badPtr
ArraySize.badSub:
	call badSub
	.globl _main
	.text
_main:
	pushq %rbp
	movq %rsp,%rbp
L.30:
	call ArraySize
#	movq %rax,%rax
	xorq %rax,%rax
#	movq %rax,%rax
#	returnSink
	popq %rbp
	ret
	.data
	.balign 8
L.25:
	.byte 65
	.byte 116
	.byte 116
	.byte 101
	.byte 109
	.byte 112
	.byte 116
	.byte 32
	.byte 116
	.byte 111
	.byte 32
	.byte 117
	.byte 115
	.byte 101
	.byte 32
	.byte 97
	.byte 32
	.byte 110
	.byte 117
	.byte 108
	.byte 108
	.byte 32
	.byte 112
	.byte 111
	.byte 105
	.byte 110
	.byte 116
	.byte 101
	.byte 114
	.byte 0
	.text
badPtr:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
L.31:
	leaq L.25(%rip),%rax
	movq %rax,%rdi
	call _puts
#	movq %rax,%rax
	movq $1,%rax
	movq %rax,%rdi
	call _exit
#	movq %rax,%rax
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret
	.data
	.balign 8
L.26:
	.byte 83
	.byte 117
	.byte 98
	.byte 115
	.byte 99
	.byte 114
	.byte 105
	.byte 112
	.byte 116
	.byte 32
	.byte 111
	.byte 117
	.byte 116
	.byte 32
	.byte 111
	.byte 102
	.byte 32
	.byte 98
	.byte 111
	.byte 117
	.byte 110
	.byte 100
	.byte 115
	.byte 0
	.text
badSub:
	pushq %rbp
	movq %rsp,%rbp
	subq $16,%rsp
L.32:
	leaq L.26(%rip),%rax
	movq %rax,%rdi
	call _puts
#	movq %rax,%rax
	movq $1,%rax
	movq %rax,%rdi
	call _exit
#	movq %rax,%rax
#	returnSink
	addq $16,%rsp
	popq %rbp
	ret
